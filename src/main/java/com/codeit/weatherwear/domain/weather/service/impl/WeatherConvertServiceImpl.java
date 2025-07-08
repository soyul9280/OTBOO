package com.codeit.weatherwear.domain.weather.service.impl;

import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.weather.assembler.WeatherAssembler;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import com.codeit.weatherwear.domain.weather.entity.WeatherApiData;
import com.codeit.weatherwear.domain.weather.service.WeatherConvertService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherConvertServiceImpl implements WeatherConvertService {

  private final WeatherAssembler weatherAssembler;

  @Override
  public List<Weather> convert(Map<String, Map<String, List<WeatherApiData>>> groupedApiData,
      Location location) {
    List<Weather> result = new ArrayList<>();

    for (Map.Entry<String, Map<String, List<WeatherApiData>>> dateEntry : groupedApiData.entrySet()) {
      convertForecastDay(dateEntry.getKey(), dateEntry.getValue(), groupedApiData,
          location).ifPresent(result::add);
    }

    return result;
  }

  private Optional<Weather> convertForecastDay(
      String fcstDate,
      Map<String, List<WeatherApiData>> timeMap,
      Map<String, Map<String, List<WeatherApiData>>> groupedApiData,
      Location location
  ) {
    List<WeatherApiData> flatList = timeMap.values().stream()
        .flatMap(List::stream)
        .collect(Collectors.toList());

    if (flatList.isEmpty()) {
      return Optional.empty();
    }

    String baseDate = flatList.get(0).getId().getBaseDate();
    if (shouldSkipForecastDate(baseDate, fcstDate)) {
      return Optional.empty();
    }

    // 카테고리 별 최신 값만 추출
    Map<String, WeatherApiData> categoryMap = toLatestCategoryMap(flatList);

    try {
      return Optional.ofNullable(
          weatherAssembler.assemble(fcstDate, baseDate, categoryMap, location, groupedApiData));
    } catch (Exception e) {
      log.error("날짜 {} → 변환 실패: {}", fcstDate, e.getMessage());
      e.printStackTrace();
      return Optional.empty();
    }

  }

  private boolean shouldSkipForecastDate(String baseDateStr, String targetDateStr) {
    LocalDate base = LocalDate.parse(baseDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
    LocalDate target = LocalDate.parse(targetDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
    return target.isAfter(base.plusDays(4));
    // 기준일로부터 4일을 넘으면 제외
  }

  private Map<String, WeatherApiData> toLatestCategoryMap(List<WeatherApiData> flatList) {
    return flatList.stream().collect(Collectors.toMap(
        data -> data.getId().getCategory(),
        Function.identity(),
        (d1, d2) -> d1.getFcstTime().compareTo(d2.getFcstTime()) <= 0 ? d1 : d2
    ));
  }
}
