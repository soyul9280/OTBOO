package com.codeit.weatherwear.domain.weather.mapper;

import com.codeit.weatherwear.domain.location.dto.LocationDto;
import com.codeit.weatherwear.domain.weather.dto.response.HumidityDto;
import com.codeit.weatherwear.domain.weather.dto.response.PrecipitationDto;
import com.codeit.weatherwear.domain.weather.dto.response.TemperatureDto;
import com.codeit.weatherwear.domain.weather.dto.response.WeatherDto;
import com.codeit.weatherwear.domain.weather.dto.response.WeatherSummaryDto;
import com.codeit.weatherwear.domain.weather.dto.response.WindSpeedDto;
import com.codeit.weatherwear.domain.weather.entity.Humidity;
import com.codeit.weatherwear.domain.weather.entity.Precipitation;
import com.codeit.weatherwear.domain.weather.entity.Temperature;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import com.codeit.weatherwear.domain.weather.entity.WindSpeed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeatherMapper {

  public WeatherDto toDto(Weather weather, LocationDto locationDto) {

    Precipitation precipitation = weather.getPrecipitation();
    Humidity humidity = weather.getHumidity();
    Temperature temperature = weather.getTemperature();
    WindSpeed windSpeed = weather.getWindSpeed();

    return WeatherDto.builder()
        .id(weather.getId())
        .forecastedAt(weather.getForecastedAt())
        .forecastAt(weather.getForecastAt())
        .location(locationDto)
        .skyStatus(weather.getSkyStatus())
        .precipitation(PrecipitationDto.builder()
            .probability(precipitation.getProbability())
            .amount(precipitation.getAmount())
            .type(precipitation.getType())
            .build())
        .humidity(HumidityDto.builder()
            .current(humidity.getCurrent())
            .comparedToDayBefore(humidity.getComparedToDayBefore())
            .build())
        .temperature(TemperatureDto.builder()
            .current(temperature.getCurrent())
            .comparedToDayBefore(temperature.getComparedToDayBefore())
            .max(temperature.getMax())
            .min(temperature.getMin())
            .build())
        .windSpeed(WindSpeedDto.builder()
            .speed(windSpeed.getSpeed())
            .asWord(windSpeed.getSpeedAsWord())
            .build())
        .build();
  }

  public WeatherSummaryDto toSummaryDto(Weather weather) {
    Precipitation precipitation = weather.getPrecipitation();
    Temperature temperature = weather.getTemperature();

    return WeatherSummaryDto.builder()
        .weatherId(weather.getId())
        .skyStatus(weather.getSkyStatus())
        .temperature(TemperatureDto.builder()
            .current(temperature.getCurrent())
            .comparedToDayBefore(temperature.getComparedToDayBefore())
            .max(temperature.getMax())
            .min(temperature.getMin())
            .build())
        .precipitation(PrecipitationDto.builder()
            .probability(precipitation.getProbability())
            .amount(precipitation.getAmount())
            .type(precipitation.getType())
            .build())
        .build();
  }
}
