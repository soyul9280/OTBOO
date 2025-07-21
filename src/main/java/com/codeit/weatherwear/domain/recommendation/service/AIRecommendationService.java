package com.codeit.weatherwear.domain.recommendation.service;

import com.codeit.weatherwear.domain.clothes.dto.response.RecommendClothesDto;
import com.codeit.weatherwear.domain.clothes.entity.Cloth;
import com.codeit.weatherwear.domain.clothes.mapper.RecommendClothesMapper;
import com.codeit.weatherwear.domain.clothes.repository.ClothRepository;
import com.codeit.weatherwear.domain.recommendation.dto.response.RecommendationDto;
import com.codeit.weatherwear.domain.recommendation.external.GeminiApiClient;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.weather.entity.Humidity;
import com.codeit.weatherwear.domain.weather.entity.Precipitation;
import com.codeit.weatherwear.domain.weather.entity.Temperature;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import com.codeit.weatherwear.domain.weather.entity.WindSpeed;
import com.codeit.weatherwear.global.storage.ThumbnailImageStorage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIRecommendationService {

  private final ClothRepository clothRepository;
  private final GeminiApiClient geminiApiClient;
  private final RecommendClothesMapper recommendClothesMapper;
  private final ThumbnailImageStorage thumbnailImageStorage;

  public List<Cloth> recommendByLLM(Weather weather, User user, List<Cloth> filtered) {
    //Gemini 프롬포트 생성 + 응답
    String text = getText(weather, user, filtered);
    String response = geminiApiClient.getInfo(text);
    //응답 파싱
    List<String> filteredNameByLLM = getOptions(response);
    log.info("[Recommendation] AI");

    return clothRepository.findAllByNames(filteredNameByLLM);
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
