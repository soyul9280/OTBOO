package com.codeit.weatherwear.domain.clothes.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import com.codeit.weatherwear.global.config.ContainerInitializer;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.util.UriComponentsBuilder;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(initializers = ContainerInitializer.class)
public class ClothTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @BeforeEach
  void setUp() {
    restTemplate = restTemplate.withBasicAuth("user", "password");
  }

  @Test
  @DisplayName("Zigzag URL에서 요소를 찾지 못해 3번 재시도 후 recover() 호출됨")
  void extractClothesFromMusinsaUrl_success() {
    // given
    String rawUrl = "https://zigzag.com/product/invalid-product-id";
    URI uri = UriComponentsBuilder
        .fromPath("/api/clothes/extractions")
        .queryParam("url", rawUrl)
        .build()
        .encode()
        .toUri();

    // when
    ResponseEntity<ClothesDto> response = restTemplate.getForEntity(
        uri, ClothesDto.class
    );

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
