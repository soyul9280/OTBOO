package com.codeit.weatherwear.domain.location.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.codeit.weatherwear.domain.location.exception.KakaoGeoApiRequestException;
import com.codeit.weatherwear.domain.location.exception.KakaoGeoApiResponseException;
import com.codeit.weatherwear.domain.location.parser.LocationApiParser;
import com.codeit.weatherwear.global.properties.LocationApiProperties;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class LocationApiClientTest {

  @InjectMocks
  LocationApiClient locationApiClient;

  @Mock
  LocationApiProperties apiProperties;

  @Mock
  LocationApiParser locationApiParser;

  @Mock
  HttpClient httpClient;

  @Mock
  HttpResponse<String> httpResponse;

  @Test
  @DisplayName("정상적인 응답이면 해당 위경도에 해당 하는 지역 이름 리스트 응답을 반환한다.")
  void getRegionName_by_geo_point() throws IOException, InterruptedException {
    // given
    double latitude = 37.5759;
    double longitude = 126.9768;
    String response = getResponseJson();
    List<String> addrList = getAddrList();

    given(apiProperties.apiUrl()).willReturn("http://test.api");
    given(apiProperties.apiKey()).willReturn("mock-test-service-key");
    given(httpClient.send(any(HttpRequest.class),
        eq(HttpResponse.BodyHandlers.ofString()))).willReturn(httpResponse);
    given(locationApiParser.parse(response)).willReturn(addrList);
    given(httpResponse.statusCode()).willReturn(200);
    given(httpResponse.body()).willReturn(response);

    // when
    List<String> result = locationApiClient.getRegionNames(latitude, longitude);

    // then
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(3);
    assertThat(result.get(0)).isEqualTo(addrList.get(0));

  }

  @Test
  @DisplayName("API 응답 시 200 응답이 아닌 다른 코드 값이 전달되어 응답 실패 예외를 전달한다")
  void getRegionNames_failed_response() throws IOException, InterruptedException {
    // given
    double latitude = 37.5759;
    double longitude = 126.9768;

    given(apiProperties.apiUrl()).willReturn("http://test.api");
    given(apiProperties.apiKey()).willReturn("mock-test-service-key");
    given(httpClient.send(any(HttpRequest.class),
        eq(HttpResponse.BodyHandlers.ofString()))).willReturn(httpResponse);
    given(httpResponse.statusCode()).willReturn(400);

    // when & then
    assertThatThrownBy(() -> locationApiClient.getRegionNames(latitude, longitude))
        .isInstanceOf(KakaoGeoApiResponseException.class);

  }

  @Test
  @DisplayName("API 응답 시 네트워크 에러 등으로 인해 오류가 발생한다")
  void getRegionNames_failed_request_error() throws IOException, InterruptedException {
    // given
    double latitude = 37.5759;
    double longitude = 126.9768;

    given(apiProperties.apiUrl()).willReturn("http://test.api");
    given(apiProperties.apiKey()).willReturn("mock-test-service-key");
    given(httpClient.send(any(HttpRequest.class),
        eq(HttpResponse.BodyHandlers.ofString()))).willThrow(new IOException("Network Error"));

    // when & then
    assertThatThrownBy(() -> locationApiClient.getRegionNames(latitude, longitude))
        .isInstanceOf(KakaoGeoApiRequestException.class);

  }

  private String getResponseJson() {
    return """
        {
        	"meta": {
        		"total_count": 1
        	},
        	"documents": [
        		{
        			"road_address": {
        				"address_name": "서울특별시 종로구 사직로 161",
        				"region_1depth_name": "서울",
        				"region_2depth_name": "종로구",
        				"region_3depth_name": "",
        				"road_name": "사직로",
        				"underground_yn": "N",
        				"main_building_no": "161",
        				"sub_building_no": "",
        				"building_name": "경복궁",
        				"zone_no": "03045"
        			},
        			"address": {
        				"address_name": "서울 종로구 세종로 1-1",
        				"region_1depth_name": "서울",
        				"region_2depth_name": "종로구",
        				"region_3depth_name": "세종로",
        				"mountain_yn": "N",
        				"main_address_no": "1",
        				"sub_address_no": "1",
        				"zip_code": ""
        			}
        		}
        	]
        }
        """;
  }

  private List<String> getAddrList() {
    return List.of("서울", "종로구", "세종로");
  }

}