package com.codeit.weatherwear.domain.feed.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.codeit.weatherwear.domain.feed.dto.response.FeedDto;
import com.codeit.weatherwear.domain.feed.entity.Feed;
import com.codeit.weatherwear.domain.feed.entity.FeedLike;
import com.codeit.weatherwear.domain.feed.exception.FeedNotFoundException;
import com.codeit.weatherwear.domain.feed.mapper.FeedLikeMapper;
import com.codeit.weatherwear.domain.feed.mapper.FeedMapper;
import com.codeit.weatherwear.domain.feed.repository.FeedLikeRepository;
import com.codeit.weatherwear.domain.feed.repository.FeedRepository;
import com.codeit.weatherwear.domain.follow.dto.UserSummaryDto;
import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.ootd.dto.response.OotdDto;
import com.codeit.weatherwear.domain.ootd.service.OotdService;
import com.codeit.weatherwear.domain.user.entity.Gender;
import com.codeit.weatherwear.domain.user.entity.Role;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.exception.UserNotFoundException;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class FeedLikeServiceImplTest {

  @InjectMocks
  private FeedLikeServiceImpl feedLikeService;

  @Mock
  private UserRepository userRepository;
  @Mock
  private FeedRepository feedRepository;
  @Mock
  private FeedLikeRepository feedLikeRepository;

  @Mock
  private OotdService ootdService;

  @Mock
  private FeedMapper feedMapper;
  @Mock
  private FeedLikeMapper feedLikeMapper;

  private UUID feedId;
  private UUID currentUserId;
  private UUID feedLikeId;

  private Feed feed;
  private FeedLike feedLike;

  private User currentUser;

  private List<OotdDto> ootds;
  private FeedDto feedDto;

  @BeforeEach
  void setUp() {
    feedId = UUID.randomUUID();
    feedLikeId = UUID.randomUUID();

    currentUserId = UUID.randomUUID();
    currentUser = createMockUser(currentUserId, mock(Location.class));
    ootds = List.of(mock(OotdDto.class));
  }

  @Test
  @DisplayName("좋아요를 성공적으로 추가한다")
  void addFeedLike_success() {
    // given
    int likeCount = 10;
    feed = createFeed(feedId, mock(User.class), mock(Weather.class), likeCount);
    feedDto = createFeedDto(likeCount + 1);
    feedLike = createFeedLike(feed);

    given(userRepository.findById(currentUserId)).willReturn(Optional.of(currentUser));
    given(feedRepository.findById(feedId)).willReturn(Optional.of(feed));
    given(feedLikeMapper.toEntity(feed, currentUser)).willReturn(feedLike);
    given(feedLikeRepository.save(feedLike)).willReturn(feedLike);
    given(ootdService.findOotdByFeedId(feedId)).willReturn(ootds);
    given(feedMapper.toDto(any(Feed.class), any(UserSummaryDto.class), any(), anyList(),
        anyBoolean())).willReturn(feedDto);

    // when
    FeedDto result = feedLikeService.addFeedLike(feedId, currentUserId);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(feed.getId());
    assertThat(result.getLikeCount()).isEqualTo(likeCount + 1);
  }

  @Test
  @DisplayName("사용자를 찾지 못해 좋아요 추가 로직을 수행하지 못한다")
  void addFeedLike_failed_cannot_find_user() {
    // given
    UUID failedId = UUID.randomUUID();

    given(userRepository.findById(failedId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> feedLikeService.addFeedLike(feedId, failedId))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  @DisplayName("피드를 찾지 못해 좋아요 추가 로직을 수행하지 못한다")
  void addFeedLike_failed_cannot_find_feed() {
    // given
    UUID failedId = UUID.randomUUID();

    given(userRepository.findById(currentUserId)).willReturn(Optional.of(currentUser));
    given(feedRepository.findById(failedId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> feedLikeService.addFeedLike(failedId, currentUserId))
        .isInstanceOf(FeedNotFoundException.class);
  }


  @Test
  @DisplayName("좋아요를 성공적으로 취소한다")
  void deleteFeedLike_success() {
    // given
    int likeCount = 10;
    feed = createFeed(feedId, mock(User.class), mock(Weather.class), likeCount);
    feedLike = createFeedLike(feed);

    given(feedRepository.findById(feed.getId())).willReturn(Optional.of(feed));
    given(feedLikeRepository.findFeedLikeByFeedIdAndUserId(feedId, currentUserId)).willReturn(
        Optional.of(feedLike));

    // when
    feedLikeService.deleteFeedLike(feedId, currentUserId);

    // then
    assertThat(feed.getLikeCount()).isEqualTo(likeCount - 1);

    verify(feedLikeRepository).delete(feedLike);
  }

  @Test
  @DisplayName("좋아요 수가 0이면 좋아요를 삭제해도 0이 리턴된다")
  void deleteFeedLike_0_success() {
    // given
    int likeCount = 0;
    feed = createFeed(feedId, mock(User.class), mock(Weather.class), likeCount);
    feedLike = createFeedLike(feed);

    given(feedRepository.findById(feed.getId())).willReturn(Optional.of(feed));
    given(feedLikeRepository.findFeedLikeByFeedIdAndUserId(feedId, currentUserId)).willReturn(
        Optional.of(feedLike));

    // when
    feedLikeService.deleteFeedLike(feedId, currentUserId);

    // then
    assertThat(feed.getLikeCount()).isEqualTo(0);

    verify(feedLikeRepository).delete(feedLike);
  }

  @Test
  @DisplayName("피드를 찾지 못해 좋아요 취소 로직을 수행하지 못한다")
  void deleteFeedLike_failed_cannot_find_feed() {
    // given
    UUID failedId = UUID.randomUUID();

    given(feedRepository.findById(failedId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> feedLikeService.deleteFeedLike(failedId, currentUserId))
        .isInstanceOf(FeedNotFoundException.class);
  }

  @Test
  @DisplayName("피드 삭제 시 해당 피드와 관련된 모든 좋아요도 함께 삭제한다")
  void deleteAllFeedLikeInFeed_success() {
    // given
    feed = mock(Feed.class);

    willDoNothing().given(feedLikeRepository).deleteAllByFeed(feed);

    // when
    feedLikeService.deleteAllFeedLikeInFeed(feed);

    // then
    verify(feedLikeRepository).deleteAllByFeed(feed);
  }

  @Test
  @DisplayName("해당 게시물에 좋아요 선택 여부를 반환한다")
  void isLikedByMe_success() {
    // given
    feed = mock(Feed.class);
    given(userRepository.findById(currentUserId)).willReturn(Optional.of(currentUser));
    given(feedLikeRepository.existsFeedLikeByFeedAndUser(feed, currentUser)).willReturn(false);

    // when
    boolean result = feedLikeService.isLikedByMe(feed, currentUserId);

    // then
    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("사용자를 찾지 못해 좋아요 선택 여부를 반환하지 못한다")
  void isLikedByMe_failed_cannot_find_user() {
    // given
    feed = mock(Feed.class);
    UUID failedId = UUID.randomUUID();

    given(userRepository.findById(failedId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> feedLikeService.isLikedByMe(feed, failedId))
        .isInstanceOf(UserNotFoundException.class);
  }

  // private method -------------

  private User createMockUser(UUID userId, Location location) {
    return User.builder()
        .id(userId)
        .email("test@example.com")
        .name("홍길동")
        .password("!password1234")
        .role(Role.USER)
        .locked(false)
        .gender(Gender.FEMALE)
        .birthDate(LocalDate.of(2000, 1, 1))
        .temperatureSensitivity(10)
        .profileImageUrl(null)
        .location(location)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();
  }

  private Feed createFeed(UUID feedId, User author, Weather weather, int likeCount) {
    Feed feed = Feed.builder()
        .author(author)
        .content("content")
        .weather(weather)
        .commentCount(0)
        .likeCount(likeCount)
        .build();
    ReflectionTestUtils.setField(feed, "id", feedId);
    ReflectionTestUtils.setField(feed, "createdAt", Instant.now());
    ReflectionTestUtils.setField(feed, "updatedAt", Instant.now());
    return feed;
  }

  private FeedDto createFeedDto(int likeCount) {
    FeedDto dto = mock(FeedDto.class);
    given(dto.getLikeCount()).willReturn(likeCount);
    given(dto.getId()).willReturn(feedId);
    return dto;
  }

  private FeedLike createFeedLike(Feed feed) {
    FeedLike feedLike = FeedLike.builder()
        .user(currentUser)
        .feed(feed)
        .build();
    ReflectionTestUtils.setField(feedLike, "id", feedLikeId);

    return feedLike;
  }

}