package com.codeit.weatherwear.domain.clothes.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.codeit.weatherwear.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.codeit.weatherwear.domain.clothes.dto.request.ClothesAttributeDefUpdateRequest;
import com.codeit.weatherwear.domain.clothes.dto.response.ClothesAttributeDefDto;
import com.codeit.weatherwear.domain.clothes.entity.Attribute;
import com.codeit.weatherwear.domain.clothes.repository.AttributeRepository;
import com.codeit.weatherwear.global.config.ContainerInitializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(initializers = ContainerInitializer.class)
public class AttributeTest {

  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private TestRestTemplate restTemplate;
  @Autowired
  private AttributeRepository repository;

  @BeforeEach
  void setUp() {
    restTemplate = restTemplate.withBasicAuth("user", "password");
  }

  @AfterEach
  void tearDown() {
    repository.deleteAll();
  }

  @Test
  @DisplayName("속성 생성 - 성공")
  void create_success() throws Exception {
    //given
    ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest(
        "사이즈",
        List.of("S", "L"));
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    String json = objectMapper.writeValueAsString(request);
    HttpEntity<String> requestEntity = new HttpEntity<>(json, headers);

    //when
    ResponseEntity<ClothesAttributeDefDto> response = restTemplate.postForEntity(
        "/api/clothes/attribute-defs", requestEntity, ClothesAttributeDefDto.class);
    //then
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody().id());
    assertEquals("사이즈", response.getBody().name());
    assertEquals(List.of("S", "L"), response.getBody().selectableValues());
  }

  @Test
  @DisplayName("속성 수정 - 성공")
  void update_success() throws Exception {
    //given
    Attribute attribute = Attribute.builder()
        .name("색상")
        .selectableValues(new ArrayList<>(List.of("빨강", "초록")))
        .build();
    repository.save(attribute);
    UUID targetId = attribute.getId();

    ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest(
        "색상",
        List.of("파랑"));
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    String json = objectMapper.writeValueAsString(request);
    HttpEntity<String> requestEntity = new HttpEntity<>(json, headers);

    //when
    ResponseEntity<ClothesAttributeDefDto> response = restTemplate.exchange(
        "/api/clothes/attribute-defs/" + targetId, HttpMethod.PATCH, requestEntity,
        ClothesAttributeDefDto.class);
    //then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody().id());
    assertEquals("색상", response.getBody().name());
    assertEquals(List.of("파랑"), response.getBody().selectableValues());
  }


}
