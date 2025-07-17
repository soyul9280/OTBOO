package com.codeit.weatherwear.domain.weather.service;

import com.codeit.weatherwear.domain.weather.entity.Weather;
import java.util.List;

public interface WeatherFetchService {

  // 날씨 fetch 및 저장까지의 총 비즈니스 로직 흐름
  List<Weather> fetchAndStoreWeather(double latitude, double longitude);

  // 날씨 fetch 및 변환까지만 수행, 저장하지 않음
  List<Weather> fetchWeather(double latitude, double longitude);

}
