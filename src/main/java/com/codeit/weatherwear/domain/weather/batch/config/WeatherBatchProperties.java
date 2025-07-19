package com.codeit.weatherwear.domain.weather.batch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "weather.batch")
public record WeatherBatchProperties(int chunkSize) {

}
