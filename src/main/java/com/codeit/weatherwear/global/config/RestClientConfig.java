package com.codeit.weatherwear.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  @Value("${gemini.api.url}")
  private String GEMINI_URL;

  @Bean
  @Primary
  public RestClient restClient() {
    return RestClient.create();
  }

  @Bean("geminiRestClient")
  public RestClient geminiRestClient(RestClient.Builder builder) {
    return builder.baseUrl(GEMINI_URL)
        .defaultHeader("Content-Type", "application/json")
        .defaultHeader("Accept", "application/json")
        .build();
  }

}
