package com.codeit.weatherwear.domain.weather.external;

import com.codeit.weatherwear.domain.weather.entity.WeatherApiData;
import com.codeit.weatherwear.domain.weather.entity.WeatherApiDataId;
import com.codeit.weatherwear.domain.weather.repository.WeatherApiDataRepository;
import com.codeit.weatherwear.global.properties.WeatherApiProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherApiClient {

  private final WeatherApiProperties apiProperties;

  private static final List<String> IGNORE_CATEGORY_LIST = List.of(
      "UUU", "VVV", "VEC", "WAV", "SNO");
  private final WeatherApiDataRepository weatherApiDataRepository;

  public void fetchAndSaveWeatherApiData() {
    ObjectMapper mapper = new ObjectMapper();
    // 1 외부 API 호출
    String responseBody = callWeatherApi(mapper, "20250701", "1100", 55, 127);
    // 2 item 필터링
    List<WeatherApiData> weatherApiData = parseItemsToEntity(mapper, responseBody);
    weatherApiDataRepository.saveAll(weatherApiData);

    // 3 Weather 엔티티 변환
  }

  private String callWeatherApi(
      ObjectMapper mapper, String baseDate, String baseTime, int nx, int ny) {
    String REQUEST_PARAM_DATA_TYPE = "JSON";
    int REQUEST_PARAM_NUM_OF_ROWS = 266;

    String requestUrl = String.format(
        "%s?serviceKey=%s&numOfRows=%d&dataType=%s&base_date=%s&base_time=%s&nx=%d&ny=%d",
        apiProperties.getApiUrl(), apiProperties.getApiServiceKey(),
        REQUEST_PARAM_NUM_OF_ROWS, REQUEST_PARAM_DATA_TYPE, baseDate, baseTime, nx, ny
    );

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(requestUrl))
        .GET()
        .build();

    try {
      HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
      String resultCode = extractResultCode(mapper, response.body());
      if (!"00".equals(resultCode)) {
        // todo: 예외 처리 필요
        throw new IllegalStateException("Weather API 응답이 정상적이지 않습니다. resultCode=" + resultCode);
      }
      return response.body();
    } catch (IOException | InterruptedException e) {
      // todo: 예외 처리 필요
      log.error("cause: {}\nmessage: {}", e.getCause(), e.getMessage());
      throw new RuntimeException();
    }
  }

  private String extractResultCode(ObjectMapper mapper, String responseBody)
      throws IOException {
    JsonNode root = mapper.readTree(responseBody);
    return root.path("response").path("header").path("resultCode").asText();
  }

  private List<WeatherApiData> parseItemsToEntity(ObjectMapper mapper,
      String responseBody) {
    try {
      JsonNode root = mapper.readTree(responseBody); // 전체 JSON
      JsonNode itemsNode = root.path("response").path("body").path("items").path("item");

      List<WeatherApiData> entities = new ArrayList<>();
      Set<String> categorySet = new HashSet<>();

      for (JsonNode item : itemsNode) {
        String category = item.path("category").asText();

        if (!categorySet.contains(category) && !IGNORE_CATEGORY_LIST.contains(category)) {
          categorySet.add(category);

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
      return entities;
    } catch (JsonProcessingException jpe) {
      // todo: 예외 처리
      throw new RuntimeException();
    }
  }
}
