package com.codeit.weatherwear.domain.weather.support;

import com.codeit.weatherwear.domain.weather.entity.WeatherApiData;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class WeatherCalculationHelper {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmm");
  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  /**
   * 날짜 + 시간 문자열을 Instant(KST)로 변환
   */
  public Instant toInstant(String date, String time) {
    return LocalDateTime.of(
        LocalDate.parse(date, DATE_FORMATTER),
        LocalTime.parse(time, TIME_FORMATTER)
    ).atZone(KST).toInstant();
  }

  public String getFcstValue(Map<String, WeatherApiData> map, String key) {
    return Optional.ofNullable(map.get(key))
        .map(WeatherApiData::getFcstValue)
        .orElse(null);
  }

  public String getBaseTime(Map<String, WeatherApiData> map, String key) {
    return Optional.ofNullable(map.get(key))
        .map(data -> data.getId().getBaseTime())
        .orElse("1100");
  }

  public Double parseDoubleOrNull(String value) {
    try {
      return value != null ? Double.parseDouble(value) : null;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /**
   * 강수량 문자열 변환 ("강수없음" → 0.0)
   */
  public double parsePrecipitation(String value) {
    if (value == null || value.equals("강수없음")) {
      return 0.0;
    }
    try {
      return Double.parseDouble(value);
    } catch (NumberFormatException e) {
      return 0.0;
    }
  }

  /**
   * 전날 같은 시간대의 예보값과 비교하여 변화량 계산
   *
   * @param current        현재 값
   * @param category       카테고리 코드 - "REH", "TMP"
   * @param fcstDate       예보일
   * @param fcstTime       예보시간
   * @param groupedApiData 날짜+시간별 그룹핑된 데이터
   * @return 전날 대비 변화량 (없을 경우 0)
   */
  public double calculateDifferenceFromPreviousDay(
      double current, String category, String fcstDate, String fcstTime,
      Map<String, Map<String, List<WeatherApiData>>> groupedApiData
  ) {
    LocalDate previousDate = LocalDate.parse(fcstDate, DATE_FORMATTER).minusDays(1);
    String previousDateStr = previousDate.format(DATE_FORMATTER);

    List<WeatherApiData> previousList = Optional.ofNullable(groupedApiData.get(previousDateStr))
        .map(map -> map.get(fcstTime))
        .orElse(Collections.emptyList());

    return previousList.stream()
        .filter(data -> category.equals(data.getId().getCategory()))
        .mapToDouble(data -> Double.parseDouble(data.getFcstValue()))
        .findFirst()
        .orElse(current) - current;
  }

  /**
   * @param categoryMap
   * @return
   */
  public String extractMinForecastTime(Map<String, WeatherApiData> categoryMap) {
    return categoryMap.values().stream()
        .map(WeatherApiData::getFcstTime)
        .min(String::compareTo)
        .orElse("1100");
  }

}
