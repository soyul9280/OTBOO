package com.codeit.weatherwear.global.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  @Value("${gemini.api.url}")
  private String geminiApiUrl;

  @Bean("geminiRestClient")
  public RestClient geminiRestClient(RestClient.Builder builder) {
    return builder.baseUrl(geminiApiUrl)
        .defaultHeader("Content-Type", "application/json")
        .defaultHeader("Accept", "application/json")
        .requestFactory(getClientHttpRequestFactory())
        .build();
  }

  private ClientHttpRequestFactory getClientHttpRequestFactory() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(Duration.ofSeconds(5));
    factory.setReadTimeout(Duration.ofSeconds(30));
    return factory;
  }

}
