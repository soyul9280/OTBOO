package com.codeit.weatherwear.domain.weather.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.weatherwear.domain.location.dto.LocationDto;
import com.codeit.weatherwear.domain.location.entity.Location;
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
import com.codeit.weatherwear.domain.weather.service.WeatherService;
import com.codeit.weatherwear.global.base.BaseControllerTest;
import com.codeit.weatherwear.global.exception.GlobalExceptionHandler;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

@WebMvcTest(WeatherController.class)
@Import({GlobalExceptionHandler.class})
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class WeatherControllerTest extends BaseControllerTest {

  @MockitoBean
  private WeatherService weatherService;

  private double latitude, longitude;

  private LocationDto locationDto;
  private Location location;

  private List<WeatherDto> weatherList;

  @BeforeEach
  void setUp() {
    latitude = 37.5759;
    longitude = 126.9768;

    locationDto = createMockLocationDto(latitude, longitude, 10, 10, getAddrList());
    location = createMockLocationByDto(locationDto);

    weatherList = createWeatherDtoList(
        List.of(
            createMockWeather(UUID.randomUUID(), location, 1),
            createMockWeather(UUID.randomUUID(), location, 2),
            createMockWeather(UUID.randomUUID(), location, 3),
            createMockWeather(UUID.randomUUID(), location, 4),
            createMockWeather(UUID.randomUUID(), location, 5)
        ));
  }

  // todo: 예외 상황 발생에 대한 처리 필요 (추후 예정)

  @Test
  @DisplayName("날씨 정보 요청에 성공한다")
  void getWeatherInfo_success() throws Exception {
    // given
    given(weatherService.getWeatherInfo(latitude, longitude)).willReturn(weatherList);

    // when & then
    mockMvc.perform(
            get("/api/weathers")
                .contentType(MediaType.APPLICATION_JSON)
                .param("latitude", String.valueOf(latitude))
                .param("longitude", String.valueOf(longitude))
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.length()").value(weatherList.size()))
        .andExpect(jsonPath("$[0].id").isNotEmpty())
        .andExpect(jsonPath("$[0].forecastAt").exists())
        .andExpect(jsonPath("$[0].forecastedAt").exists())
        .andExpect(jsonPath("$[0].location.latitude").value(latitude))
        .andExpect(jsonPath("$[0].skyStatus").value("CLEAR"));
  }

  @Test
  @DisplayName("위치 정보 요청에 성공한다")
  void getLocationInfo_success() throws Exception {
    // given
    given(weatherService.getLocationInfo(latitude, longitude)).willReturn(locationDto);

    // when & then
    mockMvc.perform(
            get("/api/weathers/location")
                .contentType(MediaType.APPLICATION_JSON)
                .param("latitude", String.valueOf(latitude))
                .param("longitude", String.valueOf(longitude))
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.latitude").value(latitude))
        .andExpect(jsonPath("$.longitude").value(longitude))
        .andExpect(jsonPath("$.x").value(10))
        .andExpect(jsonPath("$.y").value(10))
        .andExpect(jsonPath("$.locationNames[0]").value("서울"))
        .andExpect(jsonPath("$.locationNames[1]").value("종로구"))
        .andExpect(jsonPath("$.locationNames[2]").value("세종로"));
  }

  // private method ------
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

  private List<String> getAddrList() {
    return List.of("서울", "종로구", "세종로");
  }

  private LocationDto createMockLocationDto(Location location) {
    return new LocationDto(location.getLatitude(), location.getLongitude(), location.getX(),
        location.getY(), Arrays.stream(location.getName().split(" ")).toList());
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