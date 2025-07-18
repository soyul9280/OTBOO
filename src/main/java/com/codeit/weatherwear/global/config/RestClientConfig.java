package com.codeit.weatherwear.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  @Bean("geminiRestClient")
  public RestClient geminiRestClient(RestClient.Builder builder) {
    return builder.baseUrl(
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"
        )
        .defaultHeader("Content-Type", "application/json")
        .defaultHeader("Accept", "application/json")
        .build();
  }

}
