package com.codeit.weatherwear.domain.weather.service;

import com.codeit.weatherwear.domain.location.dto.LocationDto;
import com.codeit.weatherwear.domain.weather.dto.response.WeatherDto;
import java.util.List;

public interface WeatherService {

  List<WeatherDto> getWeatherInfo(double latitude, double longitude);

  LocationDto getLocationInfo(double latitude, double longitude);

}
