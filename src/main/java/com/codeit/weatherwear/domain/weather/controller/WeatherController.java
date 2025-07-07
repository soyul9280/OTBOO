package com.codeit.weatherwear.domain.weather.controller;

import com.codeit.weatherwear.domain.location.dto.LocationDto;
import com.codeit.weatherwear.domain.weather.dto.response.WeatherDto;
import com.codeit.weatherwear.domain.weather.service.WeatherService;
import java.util.List;
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

  private final WeatherService weatherService;

  @GetMapping
  public ResponseEntity<List<WeatherDto>> getWeatherInfo(
      @RequestParam(name = "latitude") double latitude,
      @RequestParam(name = "longitude") double longitude
  ) {
    return ResponseEntity.ok(
        weatherService.getWeatherInfo(latitude, longitude));
  }

  @GetMapping("/location")
  public ResponseEntity<LocationDto> getLocationInfo(
      @RequestParam(name = "latitude") double latitude,
      @RequestParam(name = "longitude") double longitude
  ) {
    return ResponseEntity.ok(weatherService.getLocationInfo(latitude, longitude));
  }
}
