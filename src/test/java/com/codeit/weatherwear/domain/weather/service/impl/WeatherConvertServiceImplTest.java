package com.codeit.weatherwear.domain.weather.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.weather.assembler.WeatherAssembler;
import com.codeit.weatherwear.domain.weather.entity.SkyStatus;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import com.codeit.weatherwear.domain.weather.entity.WeatherApiData;
import com.codeit.weatherwear.domain.weather.entity.WeatherApiDataId;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class WeatherConvertServiceImplTest {

  @InjectMocks
  private WeatherConvertServiceImpl weatherConvertService;

  @Mock
  private WeatherAssembler weatherAssembler;

  private String baseDate, baseTime, fcstDate;

  private Location location;
  private Weather weather;

  @BeforeEach
  void setUp() {
    baseDate = "20250710";
    baseTime = "1100";
    fcstDate = "20250712";

    location = mock(Location.class);
    weather = mock(Weather.class);
  }

  @Test
  @DisplayName("단기 예보 API에서 나온 데이터를 변환하여 Weather 엔티티의 형태로 반환한다")
  void convertWeatherApiData_success() {
    // given
    String categoryTmp = "TMP";
    String categorySky = "SKY";

    WeatherApiDataId id = new WeatherApiDataId(baseDate, baseTime, categoryTmp);
    WeatherApiData apiData = mock(WeatherApiData.class);
    when(apiData.getId()).thenReturn(id);
    when(apiData.getFcstValue()).thenReturn("24.3");

    WeatherApiDataId id2 = new WeatherApiDataId(baseDate, baseTime, categorySky);
    WeatherApiData apiData2 = mock(WeatherApiData.class);
    when(apiData2.getId()).thenReturn(id2);

    Map<String, List<WeatherApiData>> timeMap = Map.of(
        categoryTmp, List.of(apiData), categorySky, List.of(apiData2)
    );
    Map<String, Map<String, List<WeatherApiData>>> groupedApiData = Map.of(
        fcstDate, timeMap
    );

    when(weather.getSkyStatus()).thenReturn(SkyStatus.CLEAR);
    given(weatherAssembler.assemble(
        anyString(),
        anyString(),
        anyMap(),
        any(Location.class),
        anyMap()
    )).willReturn(weather);

    // when
    List<Weather> result = weatherConvertService.convert(groupedApiData, location);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isSameAs(weather);
    assertThat(result.get(0).getSkyStatus()).isEqualTo(weather.getSkyStatus());
  }

  @Test
  @DisplayName("Weather 엔티티로 변환 중 실패하여 빈 데이터를 반환한다")
  void convertWeatherApiData_failed() {
    // given
    String categoryTmp = "TMP";
    String categorySky = "SKY";

    WeatherApiDataId id = new WeatherApiDataId(baseDate, baseTime, categoryTmp);
    WeatherApiData apiData = mock(WeatherApiData.class);
    when(apiData.getId()).thenReturn(id);
    when(apiData.getFcstValue()).thenReturn("24.3");

    WeatherApiDataId id2 = new WeatherApiDataId(baseDate, baseTime, categorySky);
    WeatherApiData apiData2 = mock(WeatherApiData.class);
    when(apiData2.getId()).thenReturn(id2);

    Map<String, List<WeatherApiData>> timeMap = Map.of(
        categoryTmp, List.of(apiData), categorySky, List.of(apiData2)
    );
    Map<String, Map<String, List<WeatherApiData>>> groupedApiData = Map.of(
        fcstDate, timeMap
    );

    given(weatherAssembler.assemble(
        anyString(),
        anyString(),
        anyMap(),
        any(Location.class),
        anyMap()
    )).willThrow(new RuntimeException("변환 실패"));

    // when
    List<Weather> result = weatherConvertService.convert(groupedApiData, location);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("동일 카테고리 내에서 fcstTime이 더 늦은 데이터만 남는다")
  void convertWeatherApiData_latest_categoryData_only() {
    // given
    ArgumentCaptor<Map<String, WeatherApiData>> captor = ArgumentCaptor.forClass(Map.class);

    WeatherApiData tmp1 = createWeatherApiData(baseDate, baseTime, "TMP", fcstDate, "1500");
    WeatherApiData tmp2 = createWeatherApiData(baseDate, baseTime, "TMP", fcstDate, "1600"); // 최신
    WeatherApiData pop1 = createWeatherApiData(baseDate, baseTime, "POP", fcstDate, "1400"); // 최신
    WeatherApiData pop2 = createWeatherApiData(baseDate, baseTime, "POP", fcstDate, "1300");
    WeatherApiData sky = createWeatherApiData(baseDate, baseTime, "SKY", fcstDate, "1200");

    Map<String, List<WeatherApiData>> timeMap = Map.of(
        "TMP", List.of(tmp1, tmp2),
        "POP", List.of(pop1, pop2),
        "SKY", List.of(sky)
    );
    Map<String, Map<String, List<WeatherApiData>>> groupedApiData = Map.of(
        fcstDate, timeMap
    );

    // 필터링된 카테고리별 최신 데이터 맵 정의
    Map<String, WeatherApiData> expectedCategoryMap = Map.of(
        "TMP", tmp2,
        "POP", pop1,
        "SKY", sky
    );

    Location location = mock(Location.class);
    Weather weather = mock(Weather.class);

    // WeatherAssembler가 호출될 때 결과 반환
    given(weatherAssembler.assemble(
        eq(fcstDate), eq(baseDate), anyMap(), eq(location), eq(groupedApiData)
    )).willReturn(weather);

    // when
    List<Weather> result = weatherConvertService.convert(groupedApiData, location);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isSameAs(weather);

    // verify
    // weatherAssembler.assemble가 정확히 아래 인자들로 호출됐는지 검증
    // 세 번째 인자(categoryMap)를 ArgumentCaptor로 캡처
    verify(weatherAssembler).assemble(
        eq(fcstDate), eq(baseDate), captor.capture(), eq(location), eq(groupedApiData)
    );

    // 캡처된 categoryMap을 꺼내서 기대한 값과 일치하는지 확인
    Map<String, WeatherApiData> actualCategoryMap = captor.getValue();
    assertThat(actualCategoryMap)
        .containsEntry("TMP", tmp2)
        .containsEntry("POP", pop1)
        .containsEntry("SKY", sky);
  }

  // private method -------------------

  private static WeatherApiData createWeatherApiData(
      String baseDate, String baseTime, String category,
      String fcstDate, String fcstTime
  ) {
    WeatherApiDataId id = new WeatherApiDataId(baseDate, baseTime, category);
    return new WeatherApiData(id, fcstDate, fcstTime, "0", 0, 0);
  }

}