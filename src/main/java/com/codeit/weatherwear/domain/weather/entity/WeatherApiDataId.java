package com.codeit.weatherwear.domain.weather.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeatherApiDataId implements Serializable {

  private String baseDate;
  private String baseTime;
  private String category;

  @Builder
  public WeatherApiDataId(String baseDate, String baseTime, String category) {
    this.baseDate = baseDate;
    this.baseTime = baseTime;
    this.category = category;
  }
}
