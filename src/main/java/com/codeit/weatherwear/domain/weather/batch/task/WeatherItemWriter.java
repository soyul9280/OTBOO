package com.codeit.weatherwear.domain.weather.batch.task;

import com.codeit.weatherwear.domain.weather.entity.Weather;
import com.codeit.weatherwear.domain.weather.repository.WeatherRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Writer 정의
 * <p>
 * Processor를 통해 처리된 결과를 저장하는 역할
 */
@Component
@RequiredArgsConstructor
public class WeatherItemWriter {

  private final WeatherRepository weatherRepository;

  /**
   * Processor에서 전달된 List<Weather>들을 평탄화(flatMap)하여 DB에 저장
   *
   * @return ItemWriter<List < Weather>>
   */
  @Bean
  public ItemWriter<List<Weather>> weatherWriter() {
    return chunk -> {
      List<Weather> toSave = chunk.getItems().stream().flatMap(List::stream)
          .collect(Collectors.toList());
      weatherRepository.saveAll(toSave);
    };
  }
}
