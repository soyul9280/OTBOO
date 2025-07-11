package com.codeit.weatherwear.domain.weather.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.codeit.weatherwear.domain.weather.exception.WeatherApiRequestException;
import com.codeit.weatherwear.domain.weather.exception.WeatherApiResponseException;
import com.codeit.weatherwear.global.properties.WeatherApiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
class WeatherApiClientTest {

  @InjectMocks
  private WeatherApiClient weatherApiClient;

  @Mock
  private WeatherApiProperties apiProperties;

  @Mock
  private HttpClient httpClient;

  @Mock
  private HttpResponse<String> httpResponse;

  private ObjectMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new ObjectMapper();
  }

  @Test
  @DisplayName("단기 예보 API를 요청했을 때, 성공적으로 데이터를 받아온다")
  void fetchWeatherData_success() throws IOException, InterruptedException {
    // given
    String baseDate = "20250710";
    String baseTime = "0500";
    int nx = 59;
    int ny = 126;

    String responseBody = getResponseJson();

    given(apiProperties.getApiUrl()).willReturn("http://test.api");
    given(apiProperties.getApiServiceKey()).willReturn("mock-test-service-key");
    given(httpClient.send(any(HttpRequest.class),
        eq(HttpResponse.BodyHandlers.ofString()))).willReturn(httpResponse);
    given(httpResponse.body()).willReturn(responseBody);

    // when
    String result = weatherApiClient.fetchWeatherData(mapper, baseDate, baseTime, nx, ny);

    // then
    assertThat(result).isNotNull();
    assertThat(result).isEqualTo(responseBody);
  }

  @Test
  @DisplayName("단기 예보 API를 요청했을 때, 응답 코드가 00이 아닌 다른 코드로 비정상적인 응답이 전달되어 예외를 반환한다")
  void fetchWeatherData_failed_response_code_invalid() throws IOException, InterruptedException {
    // given
    String errorResponse = getErrorResponseBodyJson();

    given(apiProperties.getApiUrl()).willReturn("http://test.api");
    given(apiProperties.getApiServiceKey()).willReturn("mock-test-service-key");
    given(httpClient.send(any(HttpRequest.class),
        eq(HttpResponse.BodyHandlers.ofString()))).willReturn(httpResponse);
    given(httpResponse.body()).willReturn(errorResponse); // 응답 코드: 10 (00이 아님)

    // when & then
    assertThatThrownBy(() ->
        weatherApiClient.fetchWeatherData(mapper, "20250700", "0500", 59, 126)
    ).isInstanceOf(WeatherApiResponseException.class);
  }

  @Test
  @DisplayName("네트워크 등의 이유로 API 요청 도중 예외가 발생한다")
  void fetchWeatherData_failed_request_error() throws IOException, InterruptedException {
    // given
    given(apiProperties.getApiUrl()).willReturn("http://test.api");
    given(apiProperties.getApiServiceKey()).willReturn("mock-test-service-key");
    given(httpClient.send(any(HttpRequest.class), any())).willThrow(new InterruptedException());

    // when & then
    assertThatThrownBy(() ->
        weatherApiClient.fetchWeatherData(mapper, "20250700", "0500", 59, 126)
    ).isInstanceOf(WeatherApiRequestException.class);
  }

  // ------------
  private String getResponseJson() {
    return """
        {
        	"response": {
        		"header": {
        			"resultCode": "00",
        			"resultMsg": "NORMAL_SERVICE"
        		},
        		"body": {
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
        					}
        				]
        			},
        			"pageNo": 1,
        			"numOfRows": 3,
        			"totalCount": 907
        		}
        	}
        }
        """;
  }

  private String getErrorResponseBodyJson() {
    return """
        {
        	"response": {
        		"header": {
        			"resultCode": "10",
        			"resultMsg": "최근 3일 간의 자료만 제공합니다."
        		}
        	}
        }
        """;
  }

}