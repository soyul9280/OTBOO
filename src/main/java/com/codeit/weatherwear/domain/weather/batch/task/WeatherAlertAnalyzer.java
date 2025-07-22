package com.codeit.weatherwear.domain.weather.batch.task;

import com.codeit.weatherwear.domain.weather.entity.Precipitation;
import com.codeit.weatherwear.domain.weather.entity.PrecipitationsType;
import com.codeit.weatherwear.domain.weather.entity.Temperature;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class WeatherAlertAnalyzer {

  public WeatherAlertResult analyze(Weather todayForecast) {
    List<WeatherAlertReason> reasons = new ArrayList<>();
    // 비 예보 여부 확인
    if (isRainAlert(todayForecast)) {
      reasons.add(WeatherAlertReason.RAIN);
    }

    // 일교차 확인
    if (isTempGapAlert(todayForecast)) {
      reasons.add(WeatherAlertReason.TEMP_GAP);
    }

    return WeatherAlertResult.builder().alertNeeded(!reasons.isEmpty()).reason(reasons).build();
  }

  private boolean isRainAlert(Weather todayForecast) {
    Precipitation precipitation = todayForecast.getPrecipitation();
    return precipitation != null && precipitation.getType() != PrecipitationsType.NONE;
  }

  private boolean isTempGapAlert(Weather todayForecast) {
    Temperature temperature = todayForecast.getTemperature();
    return (temperature.getMax() - temperature.getMin()) >= 10.0;
  }

}
