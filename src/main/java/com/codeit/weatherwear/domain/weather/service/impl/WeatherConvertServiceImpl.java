package com.codeit.weatherwear.domain.weather.service.impl;

import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.weather.assembler.WeatherAssembler;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import com.codeit.weatherwear.domain.weather.entity.WeatherApiData;
import com.codeit.weatherwear.domain.weather.entity.WeatherApiDataId;
import com.codeit.weatherwear.domain.weather.service.WeatherConvertService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
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

  /**
   * 변환 수행 메서드
   *
   * @param groupedApiData 예보 날짜 -> 예보 시간 -> 해당 시간대 예보 데이터 리스트로 구성된 Map
   * @param location       위치 엔티티
   * @return Weather 엔티티 리스트
   */
  @Override
  public List<Weather> convert(Map<String, Map<String, List<WeatherApiData>>> groupedApiData,
      Location location) {
    List<Weather> result = new ArrayList<>();

    // groupedApiData 에서 날짜별 데이터를 순회하며 변환 수행 후,
    // 결과가 존재할 경우 result 에 추가
    for (Map.Entry<String, Map<String, List<WeatherApiData>>> dateEntry : groupedApiData.entrySet()) {
      convertForecastDay(dateEntry.getKey(), dateEntry.getValue(), groupedApiData,
          location).ifPresent(result::add);
    }

    return result;
  }

  /**
   * @param fcstDate       예보 날짜 (yyyyMMdd)
   * @param timeMap        시간별 WeatherApiData 리스트 맵
   * @param groupedApiData 카테고리별로 그룹화된 WeatherApiData 맵 (fcstDate -> fcstTime -> data)
   * @param location       위치 정보
   * @return 변환된 Weather 객체(Optional), 변환 불가 시 Optional.empty()
   */
  private Optional<Weather> convertForecastDay(
      String fcstDate,
      Map<String, List<WeatherApiData>> timeMap,
      Map<String, Map<String, List<WeatherApiData>>> groupedApiData,
      Location location
  ) {
    // 시간별 예보 데이터를 평탄화 하여 하나의 리스트로 만듦
    List<WeatherApiData> flatList = timeMap.values().stream()
        .flatMap(List::stream)
        .collect(Collectors.toList());

    // 예보 데이터가 없다면 변환하지 않음
    if (flatList.isEmpty()) {
      return Optional.empty();
    }

    // BaseDate (예보 기준일) 추출
    String baseDate = flatList.get(0).getId().getBaseDate();
    // 기준일(baseDate)로부터 4일을 초과하는 예보 날짜(fcstDate)는 제외
    // todo: -> 추후 비동기 등을 적용해봐도 좋을 것 같다
    if (shouldSkipForecastDate(baseDate, fcstDate)) {
      return Optional.empty();
    }

    // 카테고리 별 최신 값만 추출
    Map<String, WeatherApiData> categoryMap = toLatestCategoryMap(flatList);

    // 최고/최저기온 없으면 집어넣어주기
    if (!categoryMap.containsKey("TMN")) {
      addTmntoCategoryMap(categoryMap, flatList, baseDate, fcstDate);
    }
    if (!categoryMap.containsKey("TMX")) {
      addTmxtoCategoryMap(categoryMap, flatList, baseDate, fcstDate);
    }

    try {
      // WeatherAssembler를 이용하여 Weather 객체 생성
      return Optional.ofNullable(
          weatherAssembler.assemble(fcstDate, baseDate, categoryMap, location, groupedApiData));
    } catch (Exception e) {
      // 변환 실패 시 로그 출력 후 Optional.empty() 반환
      log.error("날짜 {} → 변환 실패: {}", fcstDate, e.getMessage());
      return Optional.empty();
    }
  }

  /**
   * 기준일(baseDate)로부터 4일을 초과하는 예보 날짜(fcstDate)는 제외<br>그 이상을 요청했을 때는 요청 시간이 너무 길어짐 (8s 이상)
   *
   * @param baseDateStr   예보기준일, yyyyMMdd 형식의 String
   * @param targetDateStr 예보대상일, yyyyMMdd 형식의 String
   * @return 기준일로부터 4일을 초과하는 예보인지 여부 (Boolean)
   */
  private boolean shouldSkipForecastDate(String baseDateStr, String targetDateStr) {
    LocalDate base = LocalDate.parse(baseDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
    LocalDate target = LocalDate.parse(targetDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
    return target.isAfter(base.plusDays(4));
    // 기준일로부터 4일을 넘으면 제외
  }

  /**
   * 예보 카테고리별로 가장 최신(fcstTime 기준) 데이터 추출
   *
   * @param flatList 하나의 날짜에 대한 모든 예보 데이터(평탄화된 WeatherApiData 리스트)
   * @return 카테고리별 최신 WeatherApiData 맵
   */
  private Map<String, WeatherApiData> toLatestCategoryMap(List<WeatherApiData> flatList) {
    return flatList.stream().collect(Collectors.toMap(
        data -> data.getId().getCategory(),
        Function.identity(),
        // 동일 카테고리 내 fcstTime이 더 늦은(최신) 데이터 선택
        (d1, d2) -> d1.getFcstTime().compareTo(d2.getFcstTime()) >= 0 ? d1 : d2
    ));
  }

  private void addTmntoCategoryMap(
      Map<String, WeatherApiData> categoryMap, List<WeatherApiData> flatList, String baseDate,
      String fcstDate) {
    // 최저 기온이 없을 때, 해당 날짜의 데이터에서 최저 기온을 뽑아서 전달
    if (!categoryMap.containsKey("TMN")) {
      OptionalDouble minTempOpt = flatList.stream()
          .filter(data -> "TMP".equals(data.getId().getCategory()))
          .mapToDouble(data -> Double.parseDouble(data.getFcstValue()))
          .min();
      minTempOpt.ifPresent(min -> {
        WeatherApiDataId id = WeatherApiDataId.builder()
            .baseDate(baseDate)
            .baseTime("0000")
            .category("TMN")
            .build();
        WeatherApiData tmnData = WeatherApiData.builder()
            .id(id)
            .fcstDate(fcstDate)
            .fcstTime("0000")
            .fcstValue(String.valueOf(min))
            .nx(flatList.get(0).getNx())
            .ny(flatList.get(0).getNy())
            .build();
        categoryMap.put("TMN", tmnData);
      });
    }
  }

  private void addTmxtoCategoryMap(
      Map<String, WeatherApiData> categoryMap, List<WeatherApiData> flatList, String baseDate,
      String fcstDate) {
    // 최고 기온이 없을 때, 해당 날짜의 데이터에서 최고 기온을 뽑아서 전달
    if (!categoryMap.containsKey("TMX")) {
      OptionalDouble maxTempOpt = flatList.stream()
          .filter(data -> "TMP".equals(data.getId().getCategory()))
          .mapToDouble(data -> Double.parseDouble(data.getFcstValue()))
          .max();
      maxTempOpt.ifPresent(max -> {
        WeatherApiDataId id = WeatherApiDataId.builder()
            .baseDate(baseDate)
            .baseTime("0000")
            .category("TMX")
            .build();
        WeatherApiData tmnData = WeatherApiData.builder()
            .id(id)
            .fcstDate(fcstDate)
            .fcstTime("0000")
            .fcstValue(String.valueOf(max))
            .nx(flatList.get(0).getNx())
            .ny(flatList.get(0).getNy())
            .build();
        categoryMap.put("TMX", tmnData);
      });
    }
  }
}

