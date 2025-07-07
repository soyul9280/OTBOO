package com.codeit.weatherwear.domain.weather.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "weather_api_data")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeatherApiData {

  @EmbeddedId
  private WeatherApiDataId id;

  private String fcstDate;
  private String fcstTime;
  private String fcstValue;

  private int nx;
  private int ny;

  @Builder
  public WeatherApiData(WeatherApiDataId id, String fcstDate, String fcstTime, String fcstValue,
      int nx, int ny) {
    this.id = id;
    this.fcstDate = fcstDate;
    this.fcstTime = fcstTime;
    this.fcstValue = fcstValue;
    this.nx = nx;
    this.ny = ny;
  }
}
