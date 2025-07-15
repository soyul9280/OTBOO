package com.codeit.weatherwear.domain.clothes.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.weatherwear.domain.clothes.dto.request.ClothesCreateRequest;
import com.codeit.weatherwear.domain.clothes.dto.request.ClothesUpdateRequest;
import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import com.codeit.weatherwear.domain.clothes.entity.ClothType;
import com.codeit.weatherwear.domain.clothes.exception.cloth.NotSupportSiteException;
import com.codeit.weatherwear.domain.clothes.repository.ClothRepository;
import com.codeit.weatherwear.domain.clothes.service.ClothService;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.global.base.BaseControllerTest;
import com.codeit.weatherwear.global.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ClothController.class)
@Import({GlobalExceptionHandler.class})
@ActiveProfiles("test")
public class ClothControllerTest extends BaseControllerTest {

  @MockitoBean
  private ClothService service;
  @MockitoBean
  private ClothRepository repository;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private MockMvc mockMvc;

  UUID testUserId;
  User testUser;

  @BeforeEach
  void setUp() {
    testUserId = UUID.randomUUID();
    testUser = User.builder()
        .id(testUserId)
        .name("test")
        .password("testpassword")
        .email("test@test.com")
        .locked(false)
        .build();
  }

  @Test
  @DisplayName("POST /api/clothes- 성공")
  void save_success() throws Exception {
    UUID clothId = UUID.randomUUID();
    ClothesCreateRequest request = new ClothesCreateRequest(testUserId, "청자켓", ClothType.TOP,
        List.of());

    MockMultipartFile image = new MockMultipartFile(
        "image", "image.jpg", MediaType.IMAGE_JPEG_VALUE, "image-content".getBytes());
    MockMultipartFile json = new MockMultipartFile(
        "request", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request));

    ClothesDto responseDto = ClothesDto.builder()
        .id(clothId)
        .ownerId(testUserId)
        .name("청자켓")
        .imageUrl("/images/cloth123.jpg")
        .type(ClothType.TOP)
        .attributes(new ArrayList<>())
        .build();
    given(service.create(any(), any())).willReturn(responseDto);

    mockMvc.perform(multipart("/api/clothes")
            .file(json)
            .file(image)
            .with(csrf())
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.imageUrl").value("/images/cloth123.jpg"))
        .andExpect(jsonPath("$.name").value(responseDto.getName()));
  }

  @Test
  @DisplayName("GET /api/clothes/extractions - 성공")
  void getFromUrl_success() throws Exception {
    UUID clothId = UUID.randomUUID();
    String url = "http://example.com/item/123";

    ClothesDto responseDto = ClothesDto.builder()
        .id(clothId)
        .ownerId(testUserId)
        .name("청자켓")
        .imageUrl("/images/cloth123.jpg")
        .type(ClothType.TOP)
        .attributes(new ArrayList<>())
        .build();
    given(service.getFromUrl(url)).willReturn(responseDto);

    mockMvc.perform(get("/api/clothes/extractions").param("url", url))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value(responseDto.getName()));
  }

  @Test
  @DisplayName("GET /api/clothes/extractions - 실패 지원하지 않는 사이트 400반환")
  void getFromUrl_unsupportedSite_returnsBadRequest() throws Exception {
    String url = "https://www.unknown.com/product/123";
    given(service.getFromUrl(url)).willThrow(new NotSupportSiteException(url));

    mockMvc.perform(get("/api/clothes/extractions")
            .param("url", "https://www.unknown.com/product/123"))
        .andExpect(status().isBadRequest());
  }


  @WithMockUser(username = "testUser", roles = "USER")
  @Test
  @DisplayName("PATCH /api/clothes/{id} - 성공")
  void update_success() throws Exception {
    UUID clothId = UUID.randomUUID();

    ClothesUpdateRequest requestDto = new ClothesUpdateRequest("수정된 이름", ClothType.TOP, List.of());

    MockMultipartFile json = new MockMultipartFile(
        "request", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(requestDto));

    MockMultipartFile image = new MockMultipartFile(
        "image", "image.jpg", MediaType.IMAGE_JPEG_VALUE, "updated-image".getBytes());

    ClothesDto responseDto = ClothesDto.builder()
        .id(clothId)
        .ownerId(testUserId)
        .name("수정된 이름")
        .imageUrl("/images/updated.jpg")
        .type(ClothType.TOP)
        .attributes(new ArrayList<>())
        .build();

    given(service.update(eq(clothId), any(), any())).willReturn(responseDto);

    mockMvc.perform(multipart("/api/clothes/" + clothId)
            .file(json)
            .file(image)
            .with(csrf())
            .with(request -> {
              request.setMethod("PATCH");
              return request;
            })
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("수정된 이름"))
        .andExpect(jsonPath("$.imageUrl").value("/images/updated.jpg"));
  }

  @WithMockUser(username = "testUser", roles = "ADMIN")
  @Test
  @DisplayName("DELETE /api/clothes/{id} - 성공")
  void delete_success() throws Exception {
    UUID clothId = UUID.randomUUID();

    willDoNothing().given(service).delete(clothId);

    mockMvc.perform(delete("/api/clothes/{id}", clothId)
            .with(csrf()))
        .andExpect(status().isNoContent());
  }


}
