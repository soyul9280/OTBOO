package com.codeit.weatherwear.domain.weather.batch.task;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WeatherAlertReason {
  RAIN("오늘은 비 예보가 있으니 주의하세요."),
  SNOW("오늘은 눈 예보가 있으니 주의하세요."),
  TEMP_GAP("오늘은 일교차가 크니 옷차림에 주의하세요.");

  private final String cause;
}
