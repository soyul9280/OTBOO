package com.codeit.weatherwear.domain.weather.controller;

import com.codeit.weatherwear.domain.location.dto.LocationDto;
import com.codeit.weatherwear.domain.weather.dto.response.WeatherDto;
import com.codeit.weatherwear.domain.weather.external.WeatherApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weathers")
@RequiredArgsConstructor
public class WeatherController {

  private final WeatherApiClient weatherApiClient;

  @GetMapping
  public ResponseEntity<WeatherDto> getWeatherInfo(
      @RequestParam double longitude,
      @RequestParam double latitude
  ) {

    return ResponseEntity.ok().build();
  }

  @GetMapping("/location")
  public ResponseEntity<LocationDto> getLocationInfo(
      @RequestParam double longitude,
      @RequestParam double latitude
  ) {

    return ResponseEntity.ok().build();
  }

  @GetMapping("/test")
  public void test() {
    weatherApiClient.fetchAndSaveWeatherApiData();
  }

}
