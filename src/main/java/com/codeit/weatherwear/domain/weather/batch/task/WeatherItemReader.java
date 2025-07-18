package com.codeit.weatherwear.domain.weather.batch.task;

import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.weather.batch.config.WeatherBatchProperties;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Reader 정의
 * <p>
 * 필요한 item(Chunk)을 불러오는 역할
 */
@Component
@RequiredArgsConstructor
public class WeatherItemReader {

  private final EntityManagerFactory emf;
  private final WeatherBatchProperties weatherBatchProperties;

  /**
   * Location 엔티티를 DB에서 페이징 방식으로 읽어오는 Reader
   *
   * @return JpaPagingItemReader<Location>
   */
  @Bean
  public ItemReader<Location> locationReader() {
    return new JpaPagingItemReaderBuilder<Location>()
        .name("locationReader")
        .entityManagerFactory(emf)
        .queryString("SELECT l FROM Location l")  // todo: 추후 쿼리 로직 수정해야 함
        .pageSize(weatherBatchProperties.chunkSize())
        .build();
  }

}
