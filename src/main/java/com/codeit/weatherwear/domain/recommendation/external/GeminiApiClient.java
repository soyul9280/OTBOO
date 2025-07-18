package com.codeit.weatherwear.domain.recommendation.external;


import com.codeit.weatherwear.domain.recommendation.dto.request.GeminiRequest;
import com.codeit.weatherwear.domain.recommendation.dto.request.GeminiRequest.Content;
import com.codeit.weatherwear.domain.recommendation.dto.request.GeminiRequest.Part;
import com.codeit.weatherwear.domain.recommendation.dto.response.GeminiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
public class GeminiApiClient {

  @Qualifier("geminiRestClient")
  private final RestClient geminiRestClient;

  @Value("${gemini.api.key}")
  private String API_KEY;

  public String getInfo(String text) {
    Part part = new Part(text);
    Content content = new Content(List.of(part));
    GeminiRequest geminiRequest = new GeminiRequest(List.of(content));
    GeminiResponse response = geminiRestClient.post()
        .header("x-google-api-key", API_KEY)
        .body(geminiRequest)
        .retrieve()
        .toEntity(GeminiResponse.class)
        .getBody();
    return response.candidates().get(0).content().parts().get(0).text();
  }

}
