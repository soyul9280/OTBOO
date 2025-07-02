package com.codeit.weatherwear.global.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "weather")
@Getter
@Setter
public class WeatherApiProperties {

  private String apiUrl;
  private String apiServiceKey;
}
