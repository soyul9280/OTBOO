package com.codeit.weatherwear.domain.weather.api;

import com.codeit.weatherwear.global.properties.WeatherApiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherApiClient {

  // todo: 작동만 되도록 아직 추상화나 패키지를 나눠두지 않았음!

  private final WeatherApiProperties apiProperties;

  public String fetchWeatherData(
      ObjectMapper mapper, String baseDate, String baseTime, int nx, int ny) {
    String REQUEST_PARAM_DATA_TYPE = "JSON";
    int REQUEST_PARAM_NUM_OF_ROWS = 1500;

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

}
