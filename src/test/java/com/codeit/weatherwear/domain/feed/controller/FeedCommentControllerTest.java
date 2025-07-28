package com.codeit.weatherwear.domain.feed.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.weatherwear.domain.feed.dto.request.FeedCommentCreateRequest;
import com.codeit.weatherwear.domain.feed.dto.request.FeedCommentGetParamRequest;
import com.codeit.weatherwear.domain.feed.dto.response.FeedCommentDto;
import com.codeit.weatherwear.domain.feed.service.FeedCommentService;
import com.codeit.weatherwear.domain.follow.dto.UserSummaryDto;
import com.codeit.weatherwear.global.base.BaseControllerTest;
import com.codeit.weatherwear.global.exception.GlobalExceptionHandler;
import com.codeit.weatherwear.global.response.PageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(FeedCommentController.class)
@Import({GlobalExceptionHandler.class})
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class FeedCommentControllerTest extends BaseControllerTest {

  @MockitoBean
  private FeedCommentService feedCommentService;

  @Autowired
  private ObjectMapper objectMapper;

  private UUID feedId;

  @BeforeEach
  void setUp() {
    feedId = UUID.randomUUID();
  }

  @Test
  @DisplayName("피드 댓글 등록 요청을 성공한다")
  void createFeedComment_success() throws Exception {
    // given
    FeedCommentCreateRequest request = FeedCommentCreateRequest.builder()
        .feedId(feedId)
        .authorId(UUID.randomUUID())
        .content("피드 댓글 등록 요청")
        .build();

    UUID feedCommentId = UUID.randomUUID();
    FeedCommentDto mockFeedCommentDto = FeedCommentDto.builder()
        .id(feedCommentId)
        .author(mock(UserSummaryDto.class))
        .content(request.getContent())
        .feedId(request.getFeedId())
        .createdAt(mock(Instant.class))
        .build();

    given(
        feedCommentService.createFeedComment(any(UUID.class), any(FeedCommentCreateRequest.class)))
        .willReturn(mockFeedCommentDto);

    // when & then
    mockMvc.perform(
            post("/api/feeds/{feedId}/comments", feedId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(feedCommentId.toString()))
        .andExpect(jsonPath("$.feedId").value(feedId.toString()))
        .andExpect(jsonPath("$.content").value(request.getContent()));
  }

  @Test
  @DisplayName("피드 댓글 조회 요청을 성공한다")
  void getFeedComments_success() throws Exception {
    // given
    FeedCommentDto feedCommentDto = mock(FeedCommentDto.class);
    when(feedCommentDto.getId()).thenReturn(UUID.randomUUID());

    int limit = 10;
    String sortBy = "createdAt";
    String sortDirection = "ASCENDING";

    PageResponse<FeedCommentDto> result = new PageResponse<>(
        List.of(feedCommentDto),
        null,
        null,
        false,
        10,
        sortBy,
        sortDirection
    );

    given(
        feedCommentService.getFeedComments(any(UUID.class), any(FeedCommentGetParamRequest.class)))
        .willReturn(result);

    // when & then
    mockMvc.perform(
            get("/api/feeds/{feedId}/comments", feedId)
                .contentType(MediaType.APPLICATION_JSON)
                .param("limit", String.valueOf(limit))
                .param("sortBy", sortBy)
                .param("sortDirection", sortDirection)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.hasNext").isBoolean())
        .andExpect(jsonPath("$.sortBy").value(sortBy))
        .andExpect(jsonPath("$.sortDirection").value(sortDirection))
        .andExpect(jsonPath("$.data[0].id").exists());
  }
}