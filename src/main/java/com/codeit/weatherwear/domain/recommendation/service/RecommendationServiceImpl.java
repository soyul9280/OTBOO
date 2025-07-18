package com.codeit.weatherwear.domain.recommendation.service;

import com.codeit.weatherwear.domain.clothes.dto.response.RecommendClothesDto;
import com.codeit.weatherwear.domain.clothes.entity.Cloth;
import com.codeit.weatherwear.domain.clothes.mapper.RecommendClothesMapper;
import com.codeit.weatherwear.domain.clothes.repository.ClothRepository;
import com.codeit.weatherwear.domain.recommendation.attributeCategory.AttributeType;
import com.codeit.weatherwear.domain.recommendation.attributeCategory.Season;
import com.codeit.weatherwear.domain.recommendation.attributeCategory.Thickness;
import com.codeit.weatherwear.domain.recommendation.dto.response.RecommendationDto;
import com.codeit.weatherwear.domain.recommendation.external.GeminiApiClient;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.exception.UserNotFoundException;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import com.codeit.weatherwear.domain.weather.entity.Humidity;
import com.codeit.weatherwear.domain.weather.entity.Precipitation;
import com.codeit.weatherwear.domain.weather.entity.Temperature;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import com.codeit.weatherwear.domain.weather.entity.WindSpeed;
import com.codeit.weatherwear.domain.weather.exception.WeatherNotFoundException;
import com.codeit.weatherwear.domain.weather.repository.WeatherRepository;
import com.codeit.weatherwear.global.storage.ThumbnailImageStorage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {

  private final UserRepository userRepository;
  private final WeatherRepository weatherRepository;
  private final ClothRepository clothRepository;
  private final FallbackRecommendationService fallbackService;
  private final GeminiApiClient geminiApiClient;
  private final RecommendClothesMapper recommendClothesMapper;
  private final ThumbnailImageStorage thumbnailImageStorage;

  /**
   * 의상 추천
   *
   * @param weatherId 날씨 ID
   * @return 추천 DTO
   */
  @Override
  public RecommendationDto recommendClothes(UUID weatherId) {
    //사용자 조회
    log.info("[Recommendation Start]");
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findByEmail(email).orElseThrow(() -> {
      log.warn("[Fail Searching Recommendation] User Not Found, Email: {}", email);
      return new UserNotFoundException();
    });
    Weather weather = weatherRepository.findById(weatherId).orElseThrow(() -> {
      log.warn("[Fail Searching Recommendation] Weather Not Found, ID: {}", weatherId);
      return new WeatherNotFoundException();
    });
    log.debug("[Recommendation] User: {}, Weather: {}", user.getId(), weather.getId());

    List<Cloth> clothes = clothRepository.findAllWithAttributesByUserId(user.getId());

    //날씨에 적당한 옷 타입마다 필터링하기
    List<Cloth> filtered = filterCloth(user, weather, clothes);
    log.info("[Recommendation] Filter Cloth Completed");

    //Gemini 프롬포트 생성 + 응답
    String text = getText(weather, user, filtered);
    String response = geminiApiClient.getInfo(text);
    //응답 파싱
    List<String> filteredNameByLLM = getOptions(response);
    try {
      List<Cloth> filteredClothes = clothRepository.findAllByNames(filteredNameByLLM);
      // DTO 변환 + 썸네일 처리
      List<RecommendClothesDto> recommendedClothes = filteredClothes.stream()
          .map(cloth -> {
            String imageUrl = cloth.getClothesImageUrl() != null
                ? thumbnailImageStorage.get(cloth.getClothesImageUrl())
                : null;
            return recommendClothesMapper.toDto(cloth, imageUrl);
          })
          .toList();

      return RecommendationDto.builder()
          .weatherId(weather.getId())
          .userId(user.getId())
          .clothes(List.copyOf(recommendedClothes))//불변성 보장
          .build();
    } catch (Exception e) {
      return fallbackService.recommend(filtered, user, weather);
    }
  }

  private List<Cloth> filterCloth(User user, Weather weather, List<Cloth> cloths) {
    //체감온도 계산
    double apparent = calculateApparentTemperature(weather);

    //민감도 보정( 사용자 민감도 가져오기, 없다면 기본값 3 )
    int sensitivity = Optional.ofNullable(user.getTemperatureSensitivity()).orElse(3);
    double adjusted = apparent + (sensitivity - 3) * 2;

    //옷 필터링
    log.info("[Recommendation] filterCloth start");
    return cloths.stream()
        .filter(c -> isSuitable(c, adjusted))
        .toList();
  }

  /**
   * 계절별 체감온도를 계산합니다. ta: 기온(°C) rh: 상대습도(%) v : 풍속(km/h), tw: 습구온도
   * <p>
   * 여름 (5월 ~ 9월) 겨울 (10월 ~ 다음해 4월)
   * <p>
   * v는 겨울철에만 사용합니다.(km/h로 변환)
   *
   * @param weather
   * @return
   */
  private double calculateApparentTemperature(Weather weather) {
    Instant forecastAt = weather.getForecastAt();
    Month month = LocalDateTime.ofInstant(forecastAt, ZoneId.systemDefault()).getMonth();
    double ta = weather.getTemperature().getCurrent();
    double rh = weather.getHumidity().getCurrent();
    double v = weather.getWindSpeed().getSpeed();

    //여름철
    if (isSummerMonth(month)) {
      double tw = calculateWetBulbTemperature(ta, rh);
      return -0.2442 + 0.55399 * tw + 0.45535 * ta
          - 0.0022 * tw * tw + 0.00278 * tw * ta + 3.0;
    }

    //겨울철 만족하기 위한 조건
    if (ta <= 10 && v >= 1.3) {
      double vk = v * 3.6; // 풍속 m/s → km/h
      return 13.12 + 0.6215 * ta - 11.37 * Math.pow(vk, 0.16)
          + 0.3965 * Math.pow(vk, 0.16) * ta;
    }

    return ta;
  }

  //여름철인지 확인
  private boolean isSummerMonth(Month month) {
    return month.getValue() >= 5 && month.getValue() <= 9;
  }

  //습구온도 계산
  private double calculateWetBulbTemperature(double ta, double rh) {
    return ta * Math.atan(0.151977 * Math.sqrt(rh + 8.313659))
        + Math.atan(ta + rh)
        - Math.atan(rh - 1.67633)
        + 0.00391838 * Math.pow(rh, 1.5) * Math.atan(0.023101 * rh)
        - 4.686035;
  }

  private boolean isSuitable(Cloth cloth, double temp) {
    // 옷의 속성 리스트를 가져와 Map 형태로 변환하여 쉽게 접근
    Map<String, String> attributeMap = toAttributeMap(cloth);

    //체감온도에 따른 계절, 두께 조건 적용
    return matchSeasonConstraint(attributeMap.get(AttributeType.SEASON.getKey()), temp)
        && matchThicknessConstraint(attributeMap.get(AttributeType.THICKNESS.getKey()), temp);
  }

  private Map<String, String> toAttributeMap(Cloth cloth) {
    return cloth.getClothesWithAttributes().stream()
        .collect(Collectors.toMap(
            attr -> attr.getAttribute().getName().toLowerCase(),
            attr -> attr.getValue().toLowerCase()
        ));
  }

  private boolean matchSeasonConstraint(String seasonAttr, double temp) {
    return Season.from(seasonAttr)
        .map(season -> {
          return switch (season) {
            case SPRING -> temp >= 5 && temp <= 20;
            case SUMMER -> temp > 20;
            case FALL -> temp >= 10 && temp <= 25;
            case WINTER -> temp < 10;
          };
        })
        .orElse(true); // season 속성이 없으면 통과
  }

  private boolean matchThicknessConstraint(String thicknessAttr, double temp) {
    return Thickness.from(thicknessAttr)
        .map(thickness -> {
          return switch (thickness) {
            case VERY_THICK -> temp <= 0;
            case THICK -> temp > 0 && temp <= 12;
            case LIGHT -> temp > 12 && temp <= 20;
            case VERY_LIGHT -> temp > 20;
          };
        })
        .orElse(true); // 두께 속성이 없으면 통과
  }

  private String getText(Weather weather, User user, List<Cloth> cloth) {
    String weatherInfo = getWeatherInfo(weather, user);
    String clothesInfo = getClothesInfo(cloth);
    String prompt = "너는 날씨와 사용자의 특성을 기반으로 옷을 추천해주는 전문가야. "
        + "다음은 더위 민감도(0: 추위 많이 탐 ~ 5: 더위 많이 탐),날씨 정보, 날씨에 맞게 1차 필터링된 옷 정보야. "
        + "각 옷은 속성 '계절'과 '두께' 조건만 고려된 상태야. "
        + "너는 이 옷들의 나머지 속성들(예: 색상, 재질, 스타일 등) 중 사용자가 선택한 값들을 종합적으로 고려하여 "
        + "현재 날씨 정보에 알맞는 옷들을 골라줘. "
        + "응답은 반드시 다음과 같은 JSON 형식의 문자열 배열로 해줘:"
        + " [\"name\"]. JSON 외에는 아무 설명도 붙이지 말고, 오직 배열만 포함시켜줘. 그리고 reason은 필요없어.";
    return weatherInfo + clothesInfo + prompt;
  }

  private String getClothesInfo(List<Cloth> cloths) {
    StringBuilder clothesInfo = new StringBuilder("옷 속성 정보 \n");
    for (Cloth cloth : cloths) {
      clothesInfo.append("이름: ").append(cloth.getName()).append("\n");
      clothesInfo.append("타입: ").append(cloth.getClothType()).append("\n");
      clothesInfo.append("속성: \n");
      cloth.getClothesWithAttributes().forEach(attr -> {
        clothesInfo.append("  - ")
            .append(attr.getAttribute().getName()).append(": ")
            .append(attr.getValue()).append("\n");
      });
      clothesInfo.append("\n");
    }
    return String.valueOf(clothesInfo.append("\n"));
  }

  private String getWeatherInfo(Weather weather, User user) {
    //날씨 조건 추출
    Temperature temperature = weather.getTemperature();
    Humidity humidity = weather.getHumidity();
    WindSpeed windSpeed = weather.getWindSpeed();
    Precipitation precipitation = weather.getPrecipitation();
    return "날씨 정보 \n"
        + "습도: " + humidity.getCurrent() + "\n"
        + "강수 타입: " + precipitation.getType() + "\n"
        + "강수량: " + precipitation.getAmount() + "\n"
        + "강수 확률: " + precipitation.getProbability() + "\n"
        + "최저 기온: " + temperature.getMin() + "\n"
        + "최고 기온: " + temperature.getMax() + "\n"
        + "현재 기온: " + temperature.getCurrent() + "\n"
        + "풍속: " + windSpeed.getSpeed() + "ms\n"
        + "바람 세기: " + windSpeed.getSpeedAsWord() + "\n"
        + "하늘 상태: " + weather.getSkyStatus() + "\n"
        + "더위 민감도: " + user.getTemperatureSensitivity() + "(1~5)\n"
        + "성별: " + user.getGender() + "\n\n";

  }

  private List<String> getOptions(String response) {
    String cleaned = response.replaceAll("(?i)```json\\s*", "")  // ```json 또는 ```JSON 제거
        .replaceAll("```", "") // 닫는 ``` 제거
        .trim(); // 양쪽 공백 제거
    ObjectMapper mapper = new ObjectMapper();
    List<String> list = null;
    try {
      list = mapper.readValue(cleaned, new TypeReference<List<String>>() {
      });
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    return list;
  }
}
