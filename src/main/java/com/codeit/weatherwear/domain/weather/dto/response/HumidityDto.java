package com.codeit.weatherwear.domain.weather.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HumidityDto {

  private final double current;
  private final double comparedToDayBefore;

}
