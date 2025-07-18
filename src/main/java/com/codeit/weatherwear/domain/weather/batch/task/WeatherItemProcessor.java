package com.codeit.weatherwear.domain.weather.batch.task;

import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import com.codeit.weatherwear.domain.weather.service.WeatherFetchService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Processor 정의
 * <p>
 * Reader에서 읽어들인 item(Chunk)을 통해 필요한 로직 수행
 */
@Component
@RequiredArgsConstructor
public class WeatherItemProcessor {

  private final WeatherFetchService weatherFetchService;

  /**
   * 각 Location에 대해 외부 API를 호출해 해당 위치의 날씨 정보를 가져옴
   * <p>
   * Reader에서 받은 Location → List<Weather>로 변환
   *
   * @return ItemProcessor<Location, List < Weather>>
   */
  @Bean
  public ItemProcessor<Location, List<Weather>> locationWeatherProcessor() {
    return location -> weatherFetchService.fetchWeather(
        location.getLatitude(), location.getLongitude()
    );
  }
}
