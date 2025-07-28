package com.codeit.weatherwear.domain.weather.batch;

import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.weather.batch.config.WeatherBatchProperties;
import com.codeit.weatherwear.domain.weather.batch.task.WeatherItemReader;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("reader-exception")
@RequiredArgsConstructor
public class FailingReaderConfig {

  private final EntityManagerFactory emf;
  private final WeatherBatchProperties weatherBatchProperties;

  @Bean
  public WeatherItemReader weatherItemReader() {
    return new WeatherItemReader(emf, weatherBatchProperties) {
      @Override
      public ItemReader<Location> locationReader() {
        return () -> {
          throw new RuntimeException("강제적으로 Reader에서 장애를 일으킴");
        };
      }
    };
  }

}
