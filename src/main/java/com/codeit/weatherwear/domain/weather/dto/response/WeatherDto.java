package com.codeit.weatherwear.domain.weather.dto.response;

import com.codeit.weatherwear.domain.location.dto.LocationDto;
import com.codeit.weatherwear.domain.weather.entity.SkyStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class WeatherDto {

  private final UUID weatherId;
  private final Instant forecastedAt;
  private final Instant forecastAt;
  private final LocationDto location;
  private final SkyStatus skyStatus;
  private final PrecipitationDto precipitation;
  private final HumidityDto humidity;
  private final TemperatureDto temperature;
  private final WindSpeedDto windSpeed;

}
