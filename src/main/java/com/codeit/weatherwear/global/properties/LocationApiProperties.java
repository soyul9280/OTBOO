package com.codeit.weatherwear.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "location")
public record LocationApiProperties(String apiUrl, String apiKey) {

}
