package com.codeit.weatherwear.domain.weather.service;

import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import com.codeit.weatherwear.domain.weather.entity.WeatherApiData;
import java.util.List;
import java.util.Map;

public interface WeatherConvertService {

  // 파싱된 데이터를 이용하여 Weather 엔티티로 변환
  List<Weather> convert(Map<String, Map<String, List<WeatherApiData>>> groupedApiData,
      Location location);
}
