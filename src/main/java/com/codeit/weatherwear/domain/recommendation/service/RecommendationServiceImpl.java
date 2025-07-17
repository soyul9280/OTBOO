package com.codeit.weatherwear.domain.recommendation.service;

import com.codeit.weatherwear.domain.clothes.entity.Cloth;
import com.codeit.weatherwear.domain.clothes.repository.ClothRepository;
import com.codeit.weatherwear.domain.recommendation.attributeCategory.AttributeType;
import com.codeit.weatherwear.domain.recommendation.attributeCategory.Season;
import com.codeit.weatherwear.domain.recommendation.attributeCategory.Thickness;
import com.codeit.weatherwear.domain.recommendation.dto.RecommendationDto;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.exception.UserNotFoundException;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import com.codeit.weatherwear.domain.weather.entity.WindSpeedType;
import com.codeit.weatherwear.domain.weather.exception.WeatherNotFoundException;
import com.codeit.weatherwear.domain.weather.repository.WeatherRepository;
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

    return fallbackService.recommend(filtered, user, weather);
  }

  private List<Cloth> filterCloth(User user, Weather weather, List<Cloth> cloths) {
    //체감온도 계산
    double apparent = calculateApparentTemperature(weather);

    //민감도 보정( 사용자 민감도 가져오기, 없다면 기본값 3 )
    int sensitivity = Optional.ofNullable(user.getTemperatureSensitivity()).orElse(3);
    double adjusted = apparent + (sensitivity - 3) * 2;

    //날씨 조건 추출
    double rainProb = weather.getPrecipitation().getProbability();
    WindSpeedType windSpeedType = weather.getWindSpeed().getSpeedAsWord();

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
            case SUMMER -> temp >= 20;
            case FALL -> temp >= 10 && temp <= 25;
            case WINTER -> temp <= 10;
          };
        })
        .orElse(true); // season 속성이 없으면 통과
  }

  private boolean matchThicknessConstraint(String thicknessAttr, double temp) {
    return Thickness.from(thicknessAttr)
        .map(thickness -> {
          return switch (thickness) {
            case VERY_THICK -> temp <= 5;
            case THICK -> temp >= 0 && temp <= 12;
            case LIGHT -> temp >= 15 && temp <= 25;
            case VERY_LIGHT -> temp >= 20;
          };
        })
        .orElse(true); // 두께 속성이 없으면 통과
  }
}
