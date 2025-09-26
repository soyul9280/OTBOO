package com.codeit.weatherwear.domain.recommendation.util;

public class WeatherBucket {

  public static String bucketizeTemp(Double temp) {
    if (temp == null) {
      return "unknown";
    }
    if (temp < 0) {
      return "below0";
    } else if (temp < 10) {
      return "0to10";
    } else if (temp < 20) {
      return "10to20";
    } else if (temp < 30) {
      return "20to30";
    } else {
      return "30plus";
    }
  }

}
