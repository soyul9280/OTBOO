package com.codeit.weatherwear.domain.feed.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.weatherwear.domain.feed.dto.request.FeedCreateRequest;
import com.codeit.weatherwear.domain.feed.dto.request.FeedGetParamRequest;
import com.codeit.weatherwear.domain.feed.dto.request.FeedUpdateRequest;
import com.codeit.weatherwear.domain.feed.dto.response.FeedDto;
import com.codeit.weatherwear.domain.feed.service.FeedService;
import com.codeit.weatherwear.domain.follow.dto.UserSummaryDto;
import com.codeit.weatherwear.domain.ootd.dto.response.OotdDto;
import com.codeit.weatherwear.domain.security.customauthentication.CustomUserDetails;
import com.codeit.weatherwear.domain.user.entity.Role;
import com.codeit.weatherwear.domain.weather.dto.response.WeatherSummaryDto;
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

@WebMvcTest(FeedController.class)
@Import({GlobalExceptionHandler.class})
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class FeedControllerTest extends BaseControllerTest {

  @MockitoBean
  private FeedService feedService;

  @Autowired
  private ObjectMapper objectMapper;

  private UUID currentUserId;
  private CustomUserDetails userDetails;

  @BeforeEach
  void setUp() {
    currentUserId = UUID.randomUUID();
    userDetails = createCustomUserDetails();
  }

  @Test
  @DisplayName("피드 등록 요청을 성공한다")
  void createFeed_success() throws Exception {
    // given
    FeedCreateRequest request = FeedCreateRequest.builder()
        .authorId(UUID.randomUUID())
        .weatherId(UUID.randomUUID())
        .clothesIds(List.of(UUID.randomUUID(), UUID.randomUUID()))
        .content("테스트 피드 내용")
        .build();

    UUID feedId = UUID.randomUUID();
    FeedDto mockFeedDto = FeedDto.builder()
        .id(feedId)
        .author(mock(UserSummaryDto.class))
        .weather(mock(WeatherSummaryDto.class))
        .ootds(List.of(mock(OotdDto.class), mock(OotdDto.class)))
        .content("content")
        .commentCount(0)
        .likeCount(0)
        .likedByMe(false)
        .build();

    given(feedService.createFeed(any(FeedCreateRequest.class), any(UUID.class)))
        .willReturn(mockFeedDto);

    // when & then
    mockMvc.perform(
            post("/api/feeds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(user(userDetails))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(feedId.toString()));
  }

  @Test
  @DisplayName("피드 목록 조회 요청을 성공한다")
  void getFeeds_success() throws Exception {
    // given
    FeedDto feedDto = mock(FeedDto.class);
    when(feedDto.getId()).thenReturn(UUID.randomUUID());

    int limit = 10;
    String sortBy = "createdAt";
    String sortDirection = "ASCENDING";

    PageResponse<FeedDto> result = new PageResponse<>(
        List.of(feedDto),
        null,
        null,
        false,
        10,
        sortBy,
        sortDirection
    );

    given(feedService.getFeedList(any(FeedGetParamRequest.class), any(UUID.class)))
        .willReturn(result);

    // when & then
    mockMvc.perform(
            get("/api/feeds")
                .contentType(MediaType.APPLICATION_JSON)
                .param("limit", String.valueOf(limit))
                .param("sortBy", sortBy)
                .param("sortDirection", sortDirection)
                .with(user(userDetails))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.hasNext").isBoolean())
        .andExpect(jsonPath("$.sortBy").value(sortBy))
        .andExpect(jsonPath("$.sortDirection").value(sortDirection))
        .andExpect(jsonPath("$.data[0].id").exists());
  }

  @Test
  @DisplayName("피드 갱신에 성공한다")
  void updateFeed_success() throws Exception {
    // given
    UUID feedId = UUID.randomUUID();

    FeedUpdateRequest request = FeedUpdateRequest.builder()
        .content("수정된 내용")
        .build();

    FeedDto mockFeedDto = FeedDto.builder()
        .id(feedId)
        .author(mock(UserSummaryDto.class))
        .weather(mock(WeatherSummaryDto.class))
        .ootds(List.of(mock(OotdDto.class), mock(OotdDto.class)))
        .content(request.getContent())
        .commentCount(0)
        .likeCount(0)
        .likedByMe(false)
        .build();

    given(feedService.updateFeed(eq(feedId), any(FeedUpdateRequest.class), any(UUID.class)))
        .willReturn(mockFeedDto);

    // when & then
    mockMvc.perform(
            patch("/api/feeds/{feedId}", feedId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(user(userDetails))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(feedId.toString()))
        .andExpect(jsonPath("$.content").value(request.getContent()));
  }

  @Test
  @DisplayName("피드 삭제에 성공한다")
  void deleteFeed_success() throws Exception {
    // given
    UUID feedId = UUID.randomUUID();
    willDoNothing().given(feedService).deleteFeed(eq(feedId), any(UUID.class));

    // when & then
    mockMvc.perform(
            delete("/api/feeds/{feedId}", feedId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(userDetails))
        )
        .andExpect(status().isNoContent());
  }

  // create CustomUserDetails ----------------

  private CustomUserDetails createCustomUserDetails() {
    return new CustomUserDetails(
        currentUserId,
        "test@example.com",
        "testpassword",
        Role.USER,
        false,
        Instant.now().plusSeconds(3000)
    );
  }

}