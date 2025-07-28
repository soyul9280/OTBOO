package com.codeit.weatherwear.domain.recommendation.external;


import com.codeit.weatherwear.domain.recommendation.dto.request.GeminiRequest;
import com.codeit.weatherwear.domain.recommendation.dto.request.GeminiRequest.Content;
import com.codeit.weatherwear.domain.recommendation.dto.request.GeminiRequest.Part;
import com.codeit.weatherwear.domain.recommendation.dto.response.GeminiResponse;
import com.codeit.weatherwear.domain.recommendation.dto.response.GeminiResponse.Candidate;
import com.codeit.weatherwear.domain.recommendation.exception.GeminiApiClientException;
import com.codeit.weatherwear.domain.recommendation.exception.GeminiApiServerException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiApiClient {

  @Qualifier("geminiRestClient")
  private final RestClient geminiRestClient;

  @Value("${gemini.api.key}")
  private String apiKey;

  public String getInfo(String text) {
    Part part = new Part(text);
    Content content = new Content(List.of(part));
    GeminiRequest geminiRequest = new GeminiRequest(List.of(content));
    
    GeminiResponse response = geminiRestClient.post()
        .header("x-goog-api-key", apiKey)
        .body(geminiRequest)
        .retrieve()
        .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
          throw new GeminiApiClientException();
        })
        .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
          throw new GeminiApiServerException();
        })
        .toEntity(GeminiResponse.class)
        .getBody();

    //응답에 문제 있을 때
    if (response == null) {
      throw new GeminiApiServerException();
    }

    //응답 후보에 문제 있을 때
    List<Candidate> candidates = response.candidates();
    if (candidates == null || candidates.isEmpty()) {
      throw new GeminiApiServerException();
    }

    log.debug("[Gemini Response Text] CandidatesSize: {}", response.candidates().size());

    return response.candidates().get(0).content().parts().get(0).text();
  }

}
