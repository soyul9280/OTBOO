package com.codeit.weatherwear.domain.clothes.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.weatherwear.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.codeit.weatherwear.domain.clothes.dto.request.ClothesAttributeDefUpdateRequest;
import com.codeit.weatherwear.domain.clothes.dto.response.ClothesAttributeDefDto;
import com.codeit.weatherwear.domain.clothes.entity.Attribute;
import com.codeit.weatherwear.domain.clothes.exception.attribute.AttributeNotFoundException;
import com.codeit.weatherwear.domain.clothes.repository.AttributeRepository;
import com.codeit.weatherwear.domain.clothes.service.AttributeService;
import com.codeit.weatherwear.global.base.BaseControllerTest;
import com.codeit.weatherwear.global.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


@WebMvcTest(AttributeController.class)
@Import({GlobalExceptionHandler.class})
@ActiveProfiles("test")
public class AttributeControllerTest extends BaseControllerTest {

  @MockitoBean
  private AttributeService service;
  @MockitoBean
  private AttributeRepository repository;
  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("POST /api/clothes/attribute-defs - 성공")
  void save_success() throws Exception {
    //given
    ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest(
        "색상",
        List.of("빨강", "파랑"));
    ClothesAttributeDefDto dto = new ClothesAttributeDefDto(UUID.randomUUID(),
        "색상", List.of("빨강", "파랑"));

    given(service.create(any(ClothesAttributeDefCreateRequest.class))).willReturn(dto);

    //when
    //then
    mockMvc.perform(
            post("/api/clothes/attribute-defs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("색상"))
        .andExpect(jsonPath("$.selectableValues[0]").value("빨강"))
        .andExpect(jsonPath("$.selectableValues[1]").value("파랑"));
  }

  @Test
  @DisplayName("POST /api/clothes/attribute-defs - 유효성 검사 실패")
  void save_fail() throws Exception {
    ClothesAttributeDefCreateRequest invalidRequest = new ClothesAttributeDefCreateRequest(
        "", List.of());

    mockMvc.perform(post("/api/clothes/attribute-defs")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("PATCH /api/clothes/attribute-defs/{definitionId}")
  void update_success() throws Exception {
    //given
    UUID id = UUID.randomUUID();
    Attribute attributes = Attribute.builder()
        .name("색상")
        .selectableValues(new ArrayList<>(List.of("빨강", "파랑")))
        .build();
    given(repository.findById(id)).willReturn(Optional.of(attributes));
    ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest("색상",
        List.of("빨강", "노랑"));

    ClothesAttributeDefDto dto = new ClothesAttributeDefDto(UUID.randomUUID(),
        "색상", List.of("빨강", "노랑"));

    given(service.update(id, request)).willReturn(dto);

    //when
    //then
    mockMvc.perform(
            patch("/api/clothes/attribute-defs/{definitionId}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("색상"))
        .andExpect(jsonPath("$.selectableValues[0]").value("빨강"))
        .andExpect(jsonPath("$.selectableValues[1]").value("노랑"));
  }

  @Test
  @DisplayName("PATCH /api/clothes/attribute-defs/{definitionId} - 존재하지 않는 ID")
  void update_notFound() throws Exception {
    UUID invalidId = UUID.randomUUID();
    ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest("색상",
        List.of("빨강", "노랑"));

    given(service.update(eq(invalidId), any())).willThrow(new AttributeNotFoundException());

    mockMvc.perform(patch("/api/clothes/attribute-defs/{definitionId}", invalidId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("속성 확인 실패"));
  }

  @Test
  @DisplayName("DELETE /api/clothes/attribute-defs/{definitionId} - 성공")
  void delete_success() throws Exception {
    UUID id = UUID.randomUUID();
    willDoNothing().given(service).delete(id);

    mockMvc.perform(delete("/api/clothes/attribute-defs/{definitionId}", id))
        .andExpect(status().isNoContent());
  }

}
