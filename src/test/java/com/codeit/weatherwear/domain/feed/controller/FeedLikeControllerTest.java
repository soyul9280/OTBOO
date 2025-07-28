package com.codeit.weatherwear.domain.feed.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.weatherwear.domain.feed.dto.response.FeedDto;
import com.codeit.weatherwear.domain.feed.service.FeedLikeService;
import com.codeit.weatherwear.domain.follow.dto.UserSummaryDto;
import com.codeit.weatherwear.domain.ootd.dto.response.OotdDto;
import com.codeit.weatherwear.domain.security.customauthentication.CustomUserDetails;
import com.codeit.weatherwear.domain.user.entity.Role;
import com.codeit.weatherwear.domain.weather.dto.response.WeatherSummaryDto;
import com.codeit.weatherwear.global.base.BaseControllerTest;
import com.codeit.weatherwear.global.exception.GlobalExceptionHandler;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(FeedLikeController.class)
@Import({GlobalExceptionHandler.class})
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class FeedLikeControllerTest extends BaseControllerTest {

  @MockitoBean
  private FeedLikeService feedLikeService;

  @Autowired
  private ObjectMapper objectMapper;

  private UUID feedId;
  private UUID currentUserId;
  private CustomUserDetails userDetails;

  @BeforeEach
  void setUp() {
    currentUserId = UUID.randomUUID();
    userDetails = createCustomUserDetails();

    feedId = UUID.randomUUID();
  }

  @Test
  @DisplayName("피드 좋아요 등록 요청을 성공한다")
  void addFeedLike_success() throws Exception {
    // given
    FeedDto mockFeedDto = FeedDto.builder()
        .id(feedId)
        .author(mock(UserSummaryDto.class))
        .weather(mock(WeatherSummaryDto.class))
        .ootds(List.of(mock(OotdDto.class), mock(OotdDto.class)))
        .content("content")
        .commentCount(0)
        .likeCount(1)
        .likedByMe(false)
        .build();

    given(feedLikeService.addFeedLike(any(UUID.class), any(UUID.class)))
        .willReturn(mockFeedDto);

    // when & then
    mockMvc.perform(
            post("/api/feeds/{feedId}/like", feedId)
                .with(user(userDetails))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(feedId.toString()))
        .andExpect(jsonPath("$.likeCount").value(mockFeedDto.getLikeCount()));
  }

  @Test
  @DisplayName("피드 좋아요 취소 요청을 성공한다")
  void deleteFeedLike_success() throws Exception {
    // given
    willDoNothing().given(feedLikeService).deleteFeedLike(any(UUID.class), any(UUID.class));

    // when & then
    mockMvc.perform(
            delete("/api/feeds/{feedId}/like", feedId)
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