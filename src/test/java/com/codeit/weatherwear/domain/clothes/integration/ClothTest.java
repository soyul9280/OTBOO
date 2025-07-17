package com.codeit.weatherwear.domain.clothes.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import com.codeit.weatherwear.global.config.TestContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestContainerConfig.class)
public class ClothTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @BeforeEach
  void setUp() {
    restTemplate = restTemplate.withBasicAuth("user", "password");
  }


  @Test
  @DisplayName("무신사 URL에서 의상 정보 추출 - 성공")
  void extractClothesFromMusinsaUrl_success() {
    // given
    String url = "https://www.musinsa.com/products/5117573";

    // when
    ResponseEntity<ClothesDto> response = restTemplate.getForEntity(
        "/api/clothes/extractions?url=" + url,
        ClothesDto.class
    );

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    ClothesDto dto = response.getBody();
    assertThat(dto).isNotNull();
    assertThat(dto.getName()).isNotEmpty();
    assertThat(dto.getImageUrl()).contains("http");
  }
}
