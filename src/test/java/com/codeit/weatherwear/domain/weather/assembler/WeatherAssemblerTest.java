package com.codeit.weatherwear.domain.weather.assembler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import com.codeit.weatherwear.domain.weather.entity.WeatherApiData;
import com.codeit.weatherwear.domain.weather.entity.WeatherApiDataId;
import com.codeit.weatherwear.domain.weather.support.WeatherCalculationHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class WeatherAssemblerTest {

  // todo: 테스트 케이스 추가 필요 (추후 진행)

  @InjectMocks
  private WeatherAssembler weatherAssembler;

  @Mock
  private WeatherCalculationHelper helper;

  private Location location;
  private Map<String, WeatherApiData> categoryMap;
  private Map<String, Map<String, List<WeatherApiData>>> groupedApiData;

  private WeatherApiData reh, tmp, tmx, tmn, sky, pty, pcp, pop, wsd;

  @BeforeEach
  void setUp() {
    location = mock(Location.class);
    categoryMap = new HashMap<>();
    groupedApiData = new HashMap<>();

    categoryMap = setCategoryMap();
    setCategoryMapReturnValue(categoryMap);
    setGroupedApiData();
  }

  @Test
  @DisplayName("assemble: 정상 입력 시 Weather 객체 생성 및 필드 매핑")
  void assemble_success() {
    // when
    Weather weather = weatherAssembler.assemble(
        "20250710", "20250709", categoryMap, location, groupedApiData
    );

    // then
    // 습도: 현재값 50.0, 전날값 48.0 → 차이 2.0
    assertThat(weather.getHumidity().getCurrent()).isEqualTo(50.0);
    assertThat(weather.getHumidity().getComparedToDayBefore()).isEqualTo(2.0);

    // 기온: 현재값 20.0, 전날값 21.0 → 차이 -1.0
    assertThat(weather.getTemperature().getCurrent()).isEqualTo(20.0);
    assertThat(weather.getTemperature().getComparedToDayBefore()).isEqualTo(-1.0);

    // 일최고/최저기온
    assertThat(weather.getTemperature().getMin()).isEqualTo(15.0);
    assertThat(weather.getTemperature().getMax()).isEqualTo(25.0);

    // 풍속
    assertThat(weather.getWindSpeed().getSpeed()).isEqualTo(1.2);
  }

  // private method ---------------

  private Map<String, WeatherApiData> setCategoryMap() {
    // 각 카테고리별 WeatherApiData Mock 세팅
    reh = mock(WeatherApiData.class);
    tmp = mock(WeatherApiData.class);
    tmx = mock(WeatherApiData.class);
    tmn = mock(WeatherApiData.class);
    sky = mock(WeatherApiData.class);
    pty = mock(WeatherApiData.class);
    pcp = mock(WeatherApiData.class);
    pop = mock(WeatherApiData.class);
    wsd = mock(WeatherApiData.class);

    categoryMap.put("REH", reh);
    categoryMap.put("TMP", tmp);
    categoryMap.put("TMX", tmx);
    categoryMap.put("TMN", tmn);
    categoryMap.put("SKY", sky);
    categoryMap.put("PTY", pty);
    categoryMap.put("PCP", pcp);
    categoryMap.put("POP", pop);
    categoryMap.put("WSD", wsd);

    return categoryMap;
  }

  private void setCategoryMapReturnValue(Map<String, WeatherApiData> categoryMap) {
    // 값 세팅 - mock 반환값 설정
    given(helper.extractMinForecastTime(categoryMap)).willReturn("0500");
    given(helper.getFcstValue(categoryMap, "REH")).willReturn("50");
    given(helper.getFcstValue(categoryMap, "TMP")).willReturn("20");
    given(helper.parseDoubleOrNull("50")).willReturn(50.0);
    given(helper.parseDoubleOrNull("20")).willReturn(20.0);
    given(helper.calculateDifferenceFromPreviousDay(50.0, "REH", "20250710", "0500",
        groupedApiData)).willReturn(2.0);
    given(helper.calculateDifferenceFromPreviousDay(20.0, "TMP", "20250710", "0500",
        groupedApiData)).willReturn(-1.0);
    given(helper.parseMinMaxTempDoubleOrNull(tmx)).willReturn(25.0);
    given(helper.parseMinMaxTempDoubleOrNull(tmn)).willReturn(15.0);
    given(helper.parseDoubleOrNull("1.2")).willReturn(1.2);

    given(sky.getFcstValue()).willReturn("1");
    given(pty.getFcstValue()).willReturn("0");
    given(pcp.getFcstValue()).willReturn("0.0");
    given(pop.getFcstValue()).willReturn("10");
    given(wsd.getFcstValue()).willReturn("1.2");

    given(helper.toInstant(anyString(), anyString())).willReturn(java.time.Instant.now());
    given(reh.getId()).willReturn(mock(WeatherApiDataId.class));
    given(reh.getId().getBaseTime()).willReturn("0500");


  }

  private void setGroupedApiData() {
    WeatherApiData prevReh = mock(WeatherApiData.class);
    WeatherApiData prevTmp = mock(WeatherApiData.class);

    // groupedApiData 형식에 맞게 삽입
    Map<String, List<WeatherApiData>> categoryMap = new HashMap<>();
    categoryMap.put("REH", List.of(prevReh));
    categoryMap.put("TMP", List.of(prevTmp));

    groupedApiData.put("20250709", categoryMap);
  }


}