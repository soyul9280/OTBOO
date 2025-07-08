package com.codeit.weatherwear.domain.weather.parser;

import com.codeit.weatherwear.domain.weather.entity.WeatherApiData;
import com.codeit.weatherwear.domain.weather.entity.WeatherApiDataId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class WeatherApiParser {

  private static final Set<String> TARGET_CATEGORIES = Set.of(
      "POP", "PTY", "PCP",  // 강수
      "REH",  // 습도
      "TMP", "TMN", "TMX",  // 기온
      "SKY",  // 하늘 상태
      "WSD"   // 풍속
  );

  public Map<String, Map<String, List<WeatherApiData>>> parse(ObjectMapper mapper,
      String responseBody) {
    try {
      JsonNode root = mapper.readTree(responseBody); // 전체 JSON
      JsonNode itemsNode = root.path("response").path("body").path("items").path("item");

      List<WeatherApiData> entities = new ArrayList<>();

      for (JsonNode item : itemsNode) {
        String category = item.path("category").asText();

        if (isTargetCategory(category)) {
          WeatherApiDataId id = WeatherApiDataId.builder()
              .baseDate(item.path("baseDate").asText())
              .baseTime(item.path("baseTime").asText())
              .category(category)
              .build();

          WeatherApiData entity = WeatherApiData.builder()
              .id(id)
              .fcstDate(item.path("fcstDate").asText())
              .fcstTime(item.path("fcstTime").asText())
              .fcstValue(item.path("fcstValue").asText())
              .nx(item.path("nx").asInt())
              .ny(item.path("ny").asInt())
              .build();

          entities.add(entity);
        }
      }

      Map<String, Map<String, List<WeatherApiData>>> grouped =
          entities.stream()
              .collect(Collectors.groupingBy(
                  WeatherApiData::getFcstDate,
                  Collectors.groupingBy(WeatherApiData::getFcstTime)
              ));

      return grouped;

    } catch (JsonProcessingException jpe) {
      // todo: 예외 처리
      throw new RuntimeException();
    }
  }

  private boolean isTargetCategory(String category) {
    return TARGET_CATEGORIES.contains(category);
  }

}
