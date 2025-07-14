package com.codeit.weatherwear.domain.weather.exception;

import com.codeit.weatherwear.global.exception.CustomException;
import com.codeit.weatherwear.global.exception.ErrorCode;

public class WeatherNotFoundException extends CustomException {

  public WeatherNotFoundException() {
    super(ErrorCode.WEATHER_NOT_FOUND);
  }
}
