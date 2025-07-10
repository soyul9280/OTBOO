package com.codeit.weatherwear.domain.weather.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.codeit.weatherwear.domain.location.dto.LocationDto;
import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.location.mapper.LocationMapper;
import com.codeit.weatherwear.domain.location.repository.LocationRepository;
import com.codeit.weatherwear.domain.location.service.LocationService;
import com.codeit.weatherwear.domain.weather.dto.response.PrecipitationDto;
import com.codeit.weatherwear.domain.weather.dto.response.TemperatureDto;
import com.codeit.weatherwear.domain.weather.dto.response.WeatherDto;
import com.codeit.weatherwear.domain.weather.dto.response.WindSpeedDto;
import com.codeit.weatherwear.domain.weather.entity.Humidity;
import com.codeit.weatherwear.domain.weather.entity.Precipitation;
import com.codeit.weatherwear.domain.weather.entity.PrecipitationsType;
import com.codeit.weatherwear.domain.weather.entity.SkyStatus;
import com.codeit.weatherwear.domain.weather.entity.Temperature;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import com.codeit.weatherwear.domain.weather.entity.WindSpeed;
import com.codeit.weatherwear.domain.weather.mapper.WeatherMapper;
import com.codeit.weatherwear.domain.weather.repository.WeatherRepository;
import com.codeit.weatherwear.domain.weather.service.WeatherFetchService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class WeatherServiceImplTest {

  @InjectMocks
  private WeatherServiceImpl weatherService;

  @Mock
  private LocationService locationService;
  @Mock
  private WeatherFetchService weatherFetchService;

  @Mock
  private LocationRepository locationRepository;
  @Mock
  private WeatherRepository weatherRepository;

  @Mock
  private LocationMapper locationMapper;
  @Mock
  private WeatherMapper weatherMapper;

  private ZoneId KST = ZoneId.of("Asia/Seoul");

  @Test
  @DisplayName("입력한 위경도에 해당하는 Location이 존재하여 위치 정보를 가져옴")
  void getWeatherInfo_success_can_find() {
    // given
    double latitude = 37.5759;
    double longitude = 126.9768;
    List<String> addrList = getAddrList();
    LocationDto locationDto = createMockLocationDto(latitude, longitude, 10, 10, addrList);
    Location location = createMockLocationByDto(locationDto);

    given(locationRepository.existsLocationByLatitudeAndLongitude(latitude, longitude)).willReturn(
        true);
    given(locationService.getLocation(latitude, longitude)).willReturn(location);
    given(locationMapper.toDto(eq(location))).willReturn(locationDto);

    // when
    LocationDto result = weatherService.getLocationInfo(latitude, longitude);

    // then
    assertThat(result).isNotNull();
    assertThat(result.locationNames()).isEqualTo(addrList);
  }

  @Test
  @DisplayName("입력한 위경도에 해당하는 Location이 존재하지 않아 위치 정보를 생성한 후 가져온다")
  void getWeatherInfo_success_can_not_find() {
    // given
    double latitude = 37.5759;
    double longitude = 126.9768;
    List<String> addrList = getAddrList();
    LocationDto locationDto = createMockLocationDto(latitude, longitude, 10, 10, addrList);
    Location location = createMockLocationByDto(locationDto);

    given(locationRepository.existsLocationByLatitudeAndLongitude(latitude, longitude)).willReturn(
        false);
    given(locationService.findOrCreateByGeoPoint(latitude, longitude)).willReturn(location);
    given(locationMapper.toDto(eq(location))).willReturn(locationDto);

    // when
    LocationDto result = weatherService.getLocationInfo(latitude, longitude);

    // then
    assertThat(result).isNotNull();
    assertThat(result.locationNames()).isEqualTo(addrList);
  }

  @Test
  @DisplayName("해당 위경도에 해당하는 위치의 예보가 존재하여 날씨 예보를 가져온다")
  void getWeatherInfo_success_forecase_exist() {
    // given
    double latitude = 37.5759;
    double longitude = 126.9768;
    List<String> addrList = getAddrList();
    LocationDto locationDto = createMockLocationDto(latitude, longitude, 10, 10, addrList);
    Location location = createMockLocationByDto(locationDto);

    List<UUID> weatherIds = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
        UUID.randomUUID(), UUID.randomUUID());
    List<Weather> weatherList = createWeatherList(location, weatherIds);
    List<WeatherDto> weatherDtoList = createWeatherDtoList(weatherList);

    Instant todayStart = LocalDate.now(KST).atStartOfDay(KST).toInstant();

    given(locationService.getLocation(latitude, longitude)).willReturn(location);
    given(locationMapper.toDto(location)).willReturn(locationDto);
    given(weatherRepository.findRecentWeathers(location, todayStart)).willReturn(weatherList);
    weatherList.sort(Comparator.comparing(Weather::getForecastAt));
    for (int i = 0; i < weatherList.size(); i++) {
      given(weatherMapper.toDto(weatherList.get(i), locationDto)).willReturn(weatherDtoList.get(i));
    }

    // when
    List<WeatherDto> result = weatherService.getWeatherInfo(latitude, longitude);

    // then
    assertThat(result).isNotNull();
    assertThat(result).hasSize(weatherDtoList.size());
    assertThat(result).containsExactlyElementsOf(weatherDtoList);
  }

  @Test
  @DisplayName("해당 위경도에 해당하는 위치의 예보가 존재하지 않아 날씨 예보를 생성 후 가져온다")
  void getWeatherInfo_success_forecast_does_not_exist() {
    // given
    double latitude = 37.5759;
    double longitude = 126.9768;
    List<String> addrList = getAddrList();
    LocationDto locationDto = createMockLocationDto(latitude, longitude, 10, 10, addrList);
    Location location = createMockLocationByDto(locationDto);

    List<UUID> weatherIds = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
        UUID.randomUUID(), UUID.randomUUID());
    List<Weather> weatherList = createWeatherList(location, weatherIds);
    List<WeatherDto> weatherDtoList = createWeatherDtoList(weatherList);

    Instant todayStart = LocalDate.now(KST).atStartOfDay(KST).toInstant();

    given(locationService.getLocation(latitude, longitude)).willReturn(location);
    given(locationMapper.toDto(location)).willReturn(locationDto);
    given(weatherRepository.findRecentWeathers(location, todayStart)).willReturn(
        Collections.emptyList());
    given(weatherFetchService.fetchAndStoreWeather(latitude, longitude)).willReturn(weatherList);
    weatherList.sort(Comparator.comparing(Weather::getForecastAt));
    for (int i = 0; i < weatherList.size(); i++) {
      given(weatherMapper.toDto(weatherList.get(i), locationDto)).willReturn(weatherDtoList.get(i));
    }

    // when
    List<WeatherDto> result = weatherService.getWeatherInfo(latitude, longitude);

    // then
    assertThat(result).isNotNull();
    assertThat(result).hasSize(weatherDtoList.size());
    assertThat(result).containsExactlyElementsOf(weatherDtoList);
  }


  // private method ---------------------
  private Location createMockLocationByDto(LocationDto locationDto) {
    String addrStr = locationDto.locationNames().stream()
        .filter(s -> s != null && !s.isBlank())
        .collect(Collectors.joining(" "));
    Location location = new Location(locationDto.latitude(), locationDto.longitude(),
        locationDto.x(), locationDto.y(), addrStr);
    ReflectionTestUtils.setField(location, "id", UUID.randomUUID());
    return location;
  }

  private LocationDto createMockLocationDto(double latitude, double longitude, int x, int y,
      List<String> addr) {
    return new LocationDto(latitude, longitude, x, y, addr);
  }

  private LocationDto createMockLocationDto(Location location) {
    return new LocationDto(location.getLatitude(), location.getLongitude(), location.getX(),
        location.getY(), Arrays.stream(location.getName().split(" ")).toList());
  }

  private List<String> getAddrList() {
    return List.of("서울", "종로구", "세종로");
  }

  private List<Weather> createWeatherList(Location location, List<UUID> weatherIds) {
    List<Weather> weatherList = new ArrayList<>();
    for (int i = 0; i < weatherIds.size(); i++) {
      weatherList.add(createMockWeather(weatherIds.get(i), location, i));
    }
    return weatherList;
  }

  private List<WeatherDto> createWeatherDtoList(List<Weather> weatherList) {
    return weatherList.stream()
        .map(weather -> createMockWeatherDto(weather.getId(), weather))
        .toList();
  }

  private Weather createMockWeather(UUID weatherId, Location location, int time) {
    Weather weather = Weather.builder()
        .location(location)
        .forecastedAt(Instant.now())
        .forecastAt(Instant.now().plusSeconds(time))
        .skyStatus(SkyStatus.CLEAR)
        .precipitation(
            Precipitation.builder()
                .type(PrecipitationsType.NONE)
                .amount(0.0)
                .probability(0.0)
                .build()
        )
        .humidity(
            Humidity.builder()
                .current(50.0)
                .comparedToDayBefore(0.0)
                .build()
        )
        .temperature(
            Temperature.builder()
                .current(20.0)
                .comparedToDayBefore(0.0)
                .min(15.0)
                .max(25.0)
                .build()
        )
        .windSpeed(
            WindSpeed.builder()
                .speed(1.2)
                .build()
        )
        .build();
    ReflectionTestUtils.setField(weather, "id", weatherId);
    return weather;
  }

  private WeatherDto createMockWeatherDto(UUID weatherId, Weather mockWeather) {
    return WeatherDto.builder()
        .id(weatherId)
        .forecastAt(mockWeather.getForecastAt())
        .forecastedAt(mockWeather.getForecastedAt())
        .location(createMockLocationDto(mockWeather.getLocation()))
        .skyStatus(mockWeather.getSkyStatus())
        .precipitation(createMockPrecipitationDto(mockWeather.getPrecipitation()))
        .temperature(createMockTemperatureDto(mockWeather.getTemperature()))
        .windSpeed(createMockWindSpeedDto(mockWeather.getWindSpeed()))
        .build();
  }

  private PrecipitationDto createMockPrecipitationDto(Precipitation precipitation) {
    return PrecipitationDto.builder()
        .type(precipitation.getType())
        .amount(precipitation.getAmount())
        .probability(precipitation.getProbability())
        .build();
  }

  private TemperatureDto createMockTemperatureDto(Temperature temperature) {
    return TemperatureDto.builder()
        .current(temperature.getCurrent())
        .comparedToDayBefore(temperature.getComparedToDayBefore())
        .min(temperature.getMin())
        .max(temperature.getMax())
        .build();
  }

  private WindSpeedDto createMockWindSpeedDto(WindSpeed windSpeed) {
    return WindSpeedDto.builder()
        .speed(windSpeed.getSpeed())
        .asWord(windSpeed.getSpeedAsWord())
        .build();
  }

}