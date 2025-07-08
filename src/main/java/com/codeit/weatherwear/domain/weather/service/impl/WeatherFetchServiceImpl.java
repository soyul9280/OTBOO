package com.codeit.weatherwear.domain.weather.service.impl;

import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.location.service.LocationService;
import com.codeit.weatherwear.domain.weather.api.WeatherApiClient;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import com.codeit.weatherwear.domain.weather.entity.WeatherApiData;
import com.codeit.weatherwear.domain.weather.parser.WeatherApiParser;
import com.codeit.weatherwear.domain.weather.repository.WeatherRepository;
import com.codeit.weatherwear.domain.weather.service.WeatherConvertService;
import com.codeit.weatherwear.domain.weather.service.WeatherFetchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherFetchServiceImpl implements WeatherFetchService {

  private final String ZONE_ID = "Asia/Seoul";
  private final List<String> ALLOWED_BASE_TIME = List.of(
      "0200", "0500", "0800", "1100", "1400", "1700", "2000", "2300"
  );
  private final LocationService locationService;
  private final WeatherConvertService weatherConvertService;
  private final WeatherRepository weatherRepository;

  private final WeatherApiClient weatherApiClient;
  private final WeatherApiParser weatherApiParser;

  @Transactional
  @Override
  public List<Weather> fetchAndStoreWeather(double latitude, double longitude) {
    Instant now = Instant.now();
    String baseDate = getBaseDate(now);
    String baseTime = getBaseTime(now);

    ObjectMapper mapper = new ObjectMapper();
    Location location = locationService.findOrCreateByGeoPoint(latitude, longitude);

    String responseBody = weatherApiClient.fetchWeatherData(mapper, baseDate, baseTime,
        location.getX(), location.getY());

    Map<String, Map<String, List<WeatherApiData>>> parsedWeatherApi = weatherApiParser.parse(mapper,
        responseBody);

    List<Weather> weatherList = weatherConvertService.convert(parsedWeatherApi, location);
    weatherRepository.saveAll(weatherList);
    return weatherList;
  }

  private String getBaseDate(Instant base) {
    return LocalDateTime.ofInstant(base, ZoneId.of("Asia/Seoul"))
        .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
  }

  private String getBaseTime(Instant base) {
    final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmm");

    LocalTime localTime = LocalDateTime.ofInstant(base, ZoneId.of(ZONE_ID)).toLocalTime();

    return ALLOWED_BASE_TIME.stream()
        .map(t -> LocalTime.parse(t, TIME_FORMATTER))
        .filter(t -> !t.isAfter(localTime))
        .min(Comparator.comparingInt(
            t -> Math.abs((int) Duration.between(localTime, t).toMinutes())))
        .map(t -> t.format(DateTimeFormatter.ofPattern("HHmm")))
        .orElse("2300");
  }

}
