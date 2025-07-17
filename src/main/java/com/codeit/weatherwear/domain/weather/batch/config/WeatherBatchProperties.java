package com.codeit.weatherwear.domain.weather.batch.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "weather.batch")
@Getter
@Setter
public class WeatherBatchProperties {

  private int chunkSize;
}
