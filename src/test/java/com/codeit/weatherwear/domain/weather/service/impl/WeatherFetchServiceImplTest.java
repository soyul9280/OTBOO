package com.codeit.weatherwear.domain.weather.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.location.service.LocationService;
import com.codeit.weatherwear.domain.weather.api.WeatherApiClient;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import com.codeit.weatherwear.domain.weather.entity.WeatherApiData;
import com.codeit.weatherwear.domain.weather.parser.WeatherApiParser;
import com.codeit.weatherwear.domain.weather.repository.WeatherRepository;
import com.codeit.weatherwear.domain.weather.service.WeatherConvertService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class WeatherFetchServiceImplTest {

  @InjectMocks
  private WeatherFetchServiceImpl weatherFetchService;

  @Mock
  private LocationService locationService;
  @Mock
  private WeatherConvertService weatherConvertService;

  @Mock
  private WeatherRepository weatherRepository;

  @Mock
  private WeatherApiClient weatherApiClient;
  @Mock
  private WeatherApiParser weatherApiParser;

  @Test
  @DisplayName("단기 예보 요청 및 파싱을 통해 날씨 데이터를 저장하는 로직이 성공적으로 수행된다")
  void fetchAndStoreWeather_success() {
    // given
    double latitude = 37.5759;
    double longitude = 126.9768;
    String responseBody = getResponseJson();

    Location location = mock(Location.class);

    Map<String, Map<String, List<WeatherApiData>>> parsedWeatherApi = new HashMap<>();
    List<Weather> weatherList = List.of(mock(Weather.class), mock(Weather.class),
        mock(Weather.class), mock(Weather.class), mock(Weather.class));

    given(locationService.findOrCreateByGeoPoint(latitude, longitude)).willReturn(location);
    given(weatherApiClient.fetchWeatherData(
        any(ObjectMapper.class),
        anyString(),
        anyString(),
        anyInt(),
        anyInt()
    )).willReturn(responseBody);
    given(weatherApiParser.parse(any(ObjectMapper.class), eq(responseBody))).willReturn(
        parsedWeatherApi);
    given(weatherConvertService.convert(parsedWeatherApi, location)).willReturn(weatherList);

    // when
    List<Weather> result = weatherFetchService.fetchAndStoreWeather(latitude, longitude);

    // then
    assertThat(result).isNotNull();

    verify(weatherRepository).saveAll(weatherList);
  }

  // private method -----------
  private String getResponseJson() {
    return """
        			"dataType": "JSON",
        			"items": {
        				"item": [
        					{
        						"baseDate": "20250710",
        						"baseTime": "0500",
        						"category": "TMP",
        						"fcstDate": "20250710",
        						"fcstTime": "0600",
        						"fcstValue": "27",
        						"nx": 59,
        						"ny": 126
        					},
        					{
        						"baseDate": "20250710",
        						"baseTime": "0500",
        						"category": "UUU",
        						"fcstDate": "20250710",
        						"fcstTime": "0600",
        						"fcstValue": "-1.6",
        						"nx": 59,
        						"ny": 126
        					},
        					{
        						"baseDate": "20250710",
        						"baseTime": "0500",
        						"category": "VVV",
        						"fcstDate": "20250710",
        						"fcstTime": "0600",
        						"fcstValue": "-0.7",
        						"nx": 59,
        						"ny": 126
        					},
        					{
        						"baseDate": "20250710",
        						"baseTime": "0500",
        						"category": "VEC",
        						"fcstDate": "20250710",
        						"fcstTime": "0600",
        						"fcstValue": "68",
        						"nx": 59,
        						"ny": 126
        					}
        				]
        			},
        			"pageNo": 1,
        			"numOfRows": 10,
        			"totalCount": 907
        """;
  }


}