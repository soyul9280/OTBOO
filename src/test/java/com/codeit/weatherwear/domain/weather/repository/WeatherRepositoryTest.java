package com.codeit.weatherwear.domain.weather.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.weatherwear.domain.location.dto.LocationDto;
import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.location.repository.LocationRepository;
import com.codeit.weatherwear.domain.weather.entity.Humidity;
import com.codeit.weatherwear.domain.weather.entity.Precipitation;
import com.codeit.weatherwear.domain.weather.entity.PrecipitationsType;
import com.codeit.weatherwear.domain.weather.entity.SkyStatus;
import com.codeit.weatherwear.domain.weather.entity.Temperature;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import com.codeit.weatherwear.domain.weather.entity.WindSpeed;
import com.codeit.weatherwear.global.config.ContainerInitializer;
import com.codeit.weatherwear.global.config.JpaConfig;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@ActiveProfiles("test")
@Import({JpaConfig.class})
@ContextConfiguration(initializers = ContainerInitializer.class)
class WeatherRepositoryTest {

  @Autowired
  private WeatherRepository weatherRepository;
  @Autowired
  private LocationRepository locationRepository;

  @Test
  @DisplayName("특정 위치와 기준 시간 이후의 Weather만 조회된다")
  void findOneDayWeathers_success() {
    // given
    double latitude = 37.5759;
    double longitude = 126.9768;

    Location location = createMockLocationByDto(createMockLocationDto(latitude, longitude));
    Location savedLocation = locationRepository.save(location);

    Instant now = Instant.now();  // 현재 시간

    // 조회 시각 (당일T00:00:00)
    LocalDate today = LocalDate.now();
    ZoneId zone = ZoneId.systemDefault();
    Instant startDay = today.atStartOfDay(zone).toInstant();

    Instant oneDayBefore = now.minusSeconds(60 * 60 * 24); // 1일 전
    Instant twoDayBefore = now.minusSeconds(60 * 60 * 24 * 2); // 2일 전
    Instant oneDayAfter = now.plusSeconds(60 * 60 * 24);   // 1일 후
    Instant twoDayAfter = now.plusSeconds(60 * 60 * 24 * 2);   // 2일 후

    Weather weather1 = createMockWeather(location, oneDayBefore);  // 1일 전
    Weather weather2 = createMockWeather(location, oneDayAfter);   // 1일 후
    Weather weather3 = createMockWeather(location, twoDayAfter);   // 2일 후
    Weather weather4 = createMockWeather(location, twoDayBefore);  // 2일 전
    Weather weather5 = createMockWeather(location, now);           // 현재 시간
    weatherRepository.saveAll(
        List.of(weather1, weather2, weather3, weather4, weather5));

    // when
    List<Weather> result = weatherRepository.findRecentWeathers(savedLocation, startDay);

    // then
    // 기준 시각 이후 데이터만 조회
    assertThat(result).containsExactlyInAnyOrder(weather2, weather3, weather5);
    assertThat(result).doesNotContain(weather1);
    assertThat(result).doesNotContain(weather4);
  }

  private Location createMockLocationByDto(LocationDto locationDto) {
    String addrStr = locationDto.locationNames().stream()
        .filter(s -> s != null && !s.isBlank())
        .collect(Collectors.joining(" "));
    Location location = new Location(locationDto.latitude(), locationDto.longitude(),
        locationDto.x(), locationDto.y(), addrStr);
    return location;
  }

  private LocationDto createMockLocationDto(double latitude, double longitude) {
    return new LocationDto(latitude, longitude, 0, 0, getAddrList());
  }

  private List<String> getAddrList() {
    return List.of("서울", "종로구", "세종로");
  }

  private Weather createMockWeather(Location location, Instant time) {
    return Weather.builder()
        .location(location)
        .forecastedAt(Instant.now())
        .forecastAt(time)
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
  }

}