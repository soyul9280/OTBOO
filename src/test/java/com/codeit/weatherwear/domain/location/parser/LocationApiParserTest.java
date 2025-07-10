package com.codeit.weatherwear.domain.location.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codeit.weatherwear.domain.location.exception.AddressJsonParseException;
import com.codeit.weatherwear.domain.location.exception.MissingAddressFieldException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class LocationApiParserTest {

  @InjectMocks
  private LocationApiParser parser;

  @Test
  @DisplayName("성공적으로 address를 통해 주소 List를 반환한다.")
  void parsing_addr_success() {
    // given
    String json = """
        {
          "documents": [
            {
              "address": {
                "region_1depth_name": "서울",
                "region_2depth_name": "송파구",
                "region_3depth_name": "신천동"
              }
            }
          ]
        }
        """;

    // when
    List<String> resultList = parser.parse(json);

    // then
    assertThat(resultList).isNotEmpty();
    assertThat(resultList).hasSize(3);
    assertThat(resultList).containsExactly("서울", "송파구", "신천동");
  }

  @Test
  @DisplayName("일부 주소 리스트가 누락된 때에는 빈 문자열을 반환한다")
  void parsing_some_field_missing() {
    // given
    String json = """
        {
          "documents": [
            {
              "address": {
                "region_1depth_name": "서울",
                "region_3depth_name": "신천동"
              }
            }
          ]
        }
        """;

    // when
    List<String> resultList = parser.parse(json);

    // then
    assertThat(resultList).isNotEmpty();
    assertThat(resultList).hasSize(3);
    assertThat(resultList).containsExactly("서울", "", "신천동");
  }

  @Test
  @DisplayName("전체 주소 리스트가 누락됐을 때에는 각각 빈 문자열을 넣어 반환한다")
  void parsing_all_field_missing() {
    // given
    String json = """
        {
          "documents": [
            {
              "address": {
              }
            }
          ]
        }
        """;

    // when
    List<String> resultList = parser.parse(json);

    // then
    assertThat(resultList).isNotEmpty();
    assertThat(resultList).hasSize(3);
    assertThat(resultList).containsExactly("", "", "");
  }

  @Test
  @DisplayName("address 노드가 없으면 MissingAddressFieldException이 발생한다")
  void parse_failed_missing_address() {
    // given
    String missingNodeJson = """
        {
          "documents": [
            {
              "abcd": {}
            }
          ]
        }
        """;

    // when & then
    assertThatThrownBy(() -> parser.parse(missingNodeJson)).isInstanceOf(
        MissingAddressFieldException.class);
  }

  @Test
  @DisplayName("잘못된 JSON 데이터라면 AddressJsonParseException이 발생한다")
  void parse_failed_invalid_json() {
    // given
    String invalidJson = "{ invalid invalidJson is HERE }";

    // when & then
    assertThatThrownBy(() -> parser.parse(invalidJson)).isInstanceOf(
        AddressJsonParseException.class);
  }
}