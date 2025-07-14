package com.codeit.weatherwear.global.config;

import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfig {

  @Bean
  public HttpClient httpClient() {
    return HttpClient.newBuilder()
        .version(Version.HTTP_2)
        .build();
  }

}
