package com.codeit.weatherwear.domain.location.api;

import com.codeit.weatherwear.domain.location.parser.LocationApiParser;
import com.codeit.weatherwear.global.properties.LocationApiProperties;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocationApiClient {

  private static final String INPUT_COORD = "WGS84";

  private final LocationApiProperties apiProperties;
  private final LocationApiParser locationApiParser;

  public List<String> getRegionNames(double latitude, double longitude) {
    String requestUrl = String.format(
        "%s?x=%f&y=%f&input_coord=%s", apiProperties.getApiUrl(), longitude, latitude, INPUT_COORD
    );

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(requestUrl))
        .header("Authorization", "KakaoAK " + apiProperties.getApiKey())
        .GET()
        .build();

    try {
      HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
      if (response.statusCode() != HttpStatus.OK.value()) {
        // todo: 예외 처리 필요
        throw new IllegalStateException("카카오 API 응답 오류: statusCode=" + response.statusCode());
      }

      return locationApiParser.parse(response.body());
    } catch (IOException | InterruptedException e) {
      // todo: 예외 처리 필요
      log.error("cause: {}\nmessage: {}", e.getCause(), e.getMessage());
      throw new RuntimeException();

    }
  }


}
