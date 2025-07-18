package com.codeit.weatherwear;

import com.codeit.weatherwear.domain.weather.batch.config.WeatherBatchProperties;
import com.codeit.weatherwear.global.properties.LocationApiProperties;
import com.codeit.weatherwear.global.properties.WeatherApiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
//
@EnableConfigurationProperties({
    LocationApiProperties.class, WeatherApiProperties.class, WeatherBatchProperties.class
})
public class WeatherwearApplication {

  public static void main(String[] args) {
    SpringApplication.run(WeatherwearApplication.class, args);
  }
}
