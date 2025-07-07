package com.codeit.weatherwear.global.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "location")
@Getter
@Setter
public class LocationApiProperties {

  private String apiUrl;
  private String apiKey;

}
