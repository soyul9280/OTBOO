package com.codeit.weatherwear.domain.weather.batch.config;

import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WeatherSkipListener implements SkipListener<Location, List<Weather>> {

  @Override
  public void onSkipInRead(Throwable t) {
    log.info("Batch in Reader: Exception Occurred - {}", t.getMessage());
  }

  @Override
  public void onSkipInWrite(List<Weather> item, Throwable t) {
    log.info("Batch in Writer: Weather - {}, Exception Occurred - {}", item, t.getMessage());
  }

  @Override
  public void onSkipInProcess(Location item, Throwable t) {
    log.info("Batch in Processor: Location - {}, Exception Occurred - {}", item.getName(),
        t.getMessage());
  }
}
