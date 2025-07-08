package com.codeit.weatherwear.domain.feed.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.codeit.weatherwear.domain.feed.dto.condition.FeedSearchCondition;
import com.codeit.weatherwear.domain.feed.dto.request.FeedCreateRequest;
import com.codeit.weatherwear.domain.feed.dto.request.FeedGetParamRequest;
import com.codeit.weatherwear.domain.feed.dto.request.FeedUpdateRequest;
import com.codeit.weatherwear.domain.feed.dto.response.FeedDto;
import com.codeit.weatherwear.domain.feed.entity.Feed;
import com.codeit.weatherwear.domain.feed.exception.FeedNotFoundException;
import com.codeit.weatherwear.domain.feed.mapper.FeedMapper;
import com.codeit.weatherwear.domain.feed.repository.FeedRepository;
import com.codeit.weatherwear.domain.feed.service.FeedCommentService;
import com.codeit.weatherwear.domain.feed.service.FeedLikeService;
import com.codeit.weatherwear.domain.follow.dto.UserSummaryDto;
import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.ootd.dto.response.OotdDto;
import com.codeit.weatherwear.domain.ootd.service.OotdService;
import com.codeit.weatherwear.domain.user.entity.Gender;
import com.codeit.weatherwear.domain.user.entity.Role;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.exception.UserNotFoundException;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import com.codeit.weatherwear.domain.weather.dto.response.PrecipitationDto;
import com.codeit.weatherwear.domain.weather.dto.response.TemperatureDto;
import com.codeit.weatherwear.domain.weather.dto.response.WeatherSummaryDto;
import com.codeit.weatherwear.domain.weather.entity.Humidity;
import com.codeit.weatherwear.domain.weather.entity.Precipitation;
import com.codeit.weatherwear.domain.weather.entity.PrecipitationsType;
import com.codeit.weatherwear.domain.weather.entity.SkyStatus;
import com.codeit.weatherwear.domain.weather.entity.Temperature;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import com.codeit.weatherwear.domain.weather.entity.WindSpeed;
import com.codeit.weatherwear.domain.weather.mapper.WeatherMapper;
import com.codeit.weatherwear.domain.weather.repository.WeatherRepository;
import com.codeit.weatherwear.global.response.PageResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@Disabled
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class FeedServiceImplTest {

  @Mock
  private UserRepository userRepository;
  @Mock
  private FeedRepository feedRepository;
  @Mock
  private WeatherRepository weatherRepository;

  @Mock
  private OotdService ootdService;
  @Mock
  private FeedCommentService feedCommentService;
  @Mock
  private FeedLikeService feedLikeService;

  @Mock
  private FeedMapper feedMapper;
  @Mock
  private WeatherMapper weatherMapper;

  @InjectMocks
  private FeedServiceImpl feedService;

  private FeedCreateRequest mockRequest;
  private UUID authorId;
  private UUID currentUserId;
  private UUID weatherId;
  private String mockContent;

  private Location mockLocation;
  private User mockAuthor;
  private UUID feedId;
  private Feed mockFeed;

  private FeedGetParamRequest mockFeedQuery;

  private WeatherSummaryDto mockWeatherDto;
  private PrecipitationDto mockPrecipitation;
  private TemperatureDto mockTemperature;
  private UserSummaryDto mockAuthorDto;
  private FeedDto mockFeedDto;

  private Feed updateMockFeed;
  private FeedDto updateFeedDto;
  private String updateContent;

  private List<UUID> clothIds;
  private UUID clothId1;
  private UUID clothId2;

  private OotdDto mockOotdDto1;
  private OotdDto mockOotdDto2;

  private Weather mockWeather;

  private FeedSearchCondition condition;

  @BeforeEach
  void setUp() {
    mockLocation = new Location(37.513068, 127.102570, 961159, 1953082, "서울 송파구 신천동");

    authorId = UUID.randomUUID();
    currentUserId = authorId;
    weatherId = UUID.randomUUID();
    clothId1 = UUID.randomUUID();
    clothId2 = UUID.randomUUID();
    clothIds = List.of(clothId1, clothId2);

    mockContent = "Mock Feed Content";

    mockAuthor = User.builder()
        .id(authorId)
        .email("test@example.com")
        .name("홍길동")
        .password("!password1234")
        .role(Role.USER)
        .locked(false)
        .gender(Gender.FEMALE)
        .birthDate(LocalDate.of(2000, 1, 1))
        .temperatureSensitivity(10)
        .profileImageUrl(null)
        .location(mockLocation)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();

    mockAuthorDto = UserSummaryDto.from(mockAuthor);

    mockRequest = FeedCreateRequest.builder()
        .authorId(authorId)
        .weatherId(weatherId)
        .clothesIds(clothIds)
        .content(mockContent)
        .build();

    mockWeather = Weather.builder()
        .location(new Location(37.513068, 127.102570, 961159, 1953082, "서울 송파구 신천동"))
        .forecastedAt(Instant.now()) // 예보 기준 시각
        .forecastAt(Instant.now().plusSeconds(3600)) // 예보 시각 (예: 1시간 후)
        .skyStatus(SkyStatus.CLEAR) // 예시: 맑음
        .precipitation(
            Precipitation.builder()
                .type(PrecipitationsType.NONE)
                .amount(0.0)
                .probability(0.0)
                .build()
        )
        .humidity(
            Humidity.builder()
                .current(50.0)
                .comparedToDayBefore(0.0)
                .build()
        )
        .temperature(
            Temperature.builder()
                .current(20.0)
                .comparedToDayBefore(0.0)
                .min(15.0)
                .max(25.0)
                .build()
        )
        .windSpeed(
            WindSpeed.builder()
                .speed(1.2)
                .build()
        )
        .build();
    ReflectionTestUtils.setField(mockWeather, "id", weatherId);

    mockWeatherDto = WeatherSummaryDto.builder()
        .weatherId(weatherId)
        .skyStatus(SkyStatus.CLEAR)
        .precipitation(mockPrecipitation)
        .temperature(mockTemperature)
        .build();

    feedId = UUID.randomUUID();
    mockFeed = Feed.builder()
        .author(mockAuthor)
        .content(mockContent)
        .weather(mockWeather)
        .commentCount(0)
        .likeCount(0)
        .build();
    ReflectionTestUtils.setField(mockFeed, "id", feedId);

    mockFeedQuery = FeedGetParamRequest.builder()
        .limit(10)
        .sortBy("createdAt")
        .sortDirection("ASCENDING")
        .build();

    condition = mockFeedQuery.toSearchCondition();

    mockPrecipitation = PrecipitationDto.builder()
        .type(PrecipitationsType.NONE)
        .amount(0.1)
        .probability(0.1)
        .build();

    mockTemperature = TemperatureDto.builder()
        .current(0.1)
        .comparedToDayBefore(0.1)
        .min(0.1)
        .max(0.1)
        .build();

    mockOotdDto1 = OotdDto.builder()
        .clothesId(clothId1)
        .name("cloth1")
        .imageUrl(null)
        .type("상의")
        .attributes(null)
        .build();
    mockOotdDto2 = OotdDto.builder()
        .clothesId(clothId2)
        .name("cloth2")
        .imageUrl(null)
        .type("하의")
        .attributes(null)
        .build();

    mockFeedDto = FeedDto.builder()
        .id(mockFeed.getId())
        .createdAt(mockFeed.getCreatedAt())
        .updatedAt(mockFeed.getUpdatedAt())
        .author(mockAuthorDto)
        .weather(mockWeatherDto)
        .ootds(List.of(mockOotdDto1, mockOotdDto2))
        .content(mockFeed.getContent())
        .commentCount(mockFeed.getCommentCount())
        .likeCount(mockFeed.getLikeCount())
        .likedByMe(false)
        .build();

    updateContent = "수정된 메시지";
    updateMockFeed = Feed.builder()
        .author(mockAuthor)
        .content(updateContent)
        .weather(mockWeather)
        .commentCount(0)
        .likeCount(0)
        .build();
    ReflectionTestUtils.setField(updateMockFeed, "id", feedId);

    updateFeedDto = FeedDto.builder()
        .id(updateMockFeed.getId())
        .createdAt(updateMockFeed.getCreatedAt())
        .updatedAt(updateMockFeed.getUpdatedAt())
        .author(mockAuthorDto)
        .weather(mockWeatherDto)
        .ootds(null)
        .content(updateMockFeed.getContent())
        .commentCount(updateMockFeed.getCommentCount())
        .likeCount(updateMockFeed.getLikeCount())
        .likedByMe(false)
        .build();

    ReflectionTestUtils.setField(mockFeed, "id", feedId);
  }

  @Test
  @DisplayName("Feed를 성공적으로 생성하여 FeedDto를 리턴합니다.")
  void createFeed_success() {
    // given
    given(userRepository.findById(authorId)).willReturn(Optional.ofNullable(mockAuthor));
    given(weatherRepository.findById(weatherId)).willReturn(Optional.ofNullable(mockWeather));
    given(feedMapper.toEntity(mockAuthor, mockWeather, mockRequest)).willReturn(mockFeed);
    given(feedRepository.save(mockFeed)).willReturn(mockFeed);
    given(ootdService.createOotdList(eq(mockFeed), eq(mockRequest.getClothesIds()))).willReturn(
        List.of(mockOotdDto1, mockOotdDto2));
    given(weatherMapper.toSummaryDto(eq(mockWeather))).willReturn(mockWeatherDto);
    given(feedMapper.toDto(eq(mockFeed), eq(mockAuthorDto), any(WeatherSummaryDto.class),
        eq(List.of(mockOotdDto1, mockOotdDto2)),
        eq(false))).willReturn(mockFeedDto);

    // when
    FeedDto result = feedService.createFeed(mockRequest, currentUserId);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(mockFeed.getId());
    assertThat(result.getAuthor().userId()).isEqualTo(mockAuthorDto.userId());
    assertThat(result.getOotds()).isNotNull();
    assertThat(result.isLikedByMe()).isFalse();

    // verify
    verify(userRepository).findById(authorId);
    verify(feedMapper).toEntity(mockAuthor, mockWeather, mockRequest);
    verify(feedRepository).save(mockFeed);
    verify(feedMapper).toDto(eq(mockFeed), eq(mockAuthorDto), any(WeatherSummaryDto.class),
        eq(List.of(mockOotdDto1, mockOotdDto2)), eq(false));
  }

  @Test
  @DisplayName("사용자를 찾지 못해 피드 생성에 실패합니다.")
  void createFeed_failed_cannot_find_user() {
    // given
    UUID failedId = UUID.randomUUID();
    FeedCreateRequest failedRequest = FeedCreateRequest.builder()
        .authorId(failedId)
        .weatherId(weatherId)
        .clothesIds(clothIds)
        .content("실패 테스트")
        .build();

    given(userRepository.findById(failedId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> feedService.createFeed(failedRequest, failedId))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  @DisplayName("전체 피드 리스트를 성공적으로 불러와 PageResponse로 전달한다")
  void getFeedList_success() {
    // given
    List<Feed> feedList = List.of(mockFeed);

    given(feedRepository.searchFeeds(any(FeedSearchCondition.class),
        anyInt())).willReturn(feedList);
    given(ootdService.findOotdByFeedId(eq(mockFeed.getId()))).willReturn(
        List.of(mockOotdDto1, mockOotdDto2));
    given(feedLikeService.isLikedByMe(mockFeed, currentUserId)).willReturn(false);
    given(feedMapper.toDto(
        eq(mockFeed),
        eq(mockAuthorDto),
        any(WeatherSummaryDto.class),
        anyList(),
        eq(false)
    )).willReturn(mockFeedDto);

    // when
    PageResponse<FeedDto> resultList = feedService.getFeedList(mockFeedQuery, currentUserId);

    // then
    assertThat(resultList).isNotNull();
    assertThat(resultList.data()).hasSize(1);
    assertThat(resultList.data().get(0).getOotds()).isNotNull();
    assertThat(resultList.data().get(0)).isEqualTo(mockFeedDto);
    assertThat(resultList.hasNext()).isFalse();
    assertThat(resultList.sortBy()).isEqualTo(condition.getSortBy());
    assertThat(resultList.sortDirection()).isEqualTo(condition.getSortDirection().name());

    // verify
    verify(feedRepository).searchFeeds(any(FeedSearchCondition.class), anyInt());
    verify(feedLikeService).isLikedByMe(mockFeed, currentUserId);
    verify(feedMapper, times(feedList.size())).toDto(eq(mockFeed), eq(mockAuthorDto),
        any(WeatherSummaryDto.class), eq(List.of(mockOotdDto1, mockOotdDto2)), eq(false));
  }

  @Test
  @DisplayName("피드를 성공적으로 수정한다")
  void updateFeed_success() {
    // given
    FeedUpdateRequest updateRequest = FeedUpdateRequest.builder().content(updateContent).build();

    given(feedRepository.findById(feedId)).willReturn(Optional.of(mockFeed));
    given(ootdService.findOotdByFeedId(eq(mockFeed.getId()))).willReturn(
        List.of(mockOotdDto1, mockOotdDto2));
    given(feedLikeService.isLikedByMe(mockFeed, currentUserId)).willReturn(false);
    given(feedMapper.toDto(eq(mockFeed), eq(mockAuthorDto), any(WeatherSummaryDto.class),
        eq(List.of(mockOotdDto1, mockOotdDto2)),
        eq(false))).willReturn(updateFeedDto);

    // when
    FeedDto result = feedService.updateFeed(feedId, updateRequest, currentUserId);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isEqualTo(updateContent);
  }

  @Test
  @DisplayName("피드를 조회하지 못 해 피드 수정을 실패한다")
  void updateFeed_failed_cannot_find_feed() {
    // given
    UUID failedId = UUID.randomUUID();
    FeedUpdateRequest failedRequest = FeedUpdateRequest.builder()
        .content("실패 테스트")
        .build();

    given(feedRepository.findById(failedId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> feedService.updateFeed(failedId, failedRequest, currentUserId))
        .isInstanceOf(FeedNotFoundException.class);
  }

  @Test
  @DisplayName("피드 삭제를 성공한다")
  void deleteFeed_success() {
    // given
    given(feedRepository.findById(feedId)).willReturn(Optional.of(mockFeed));
    given(feedLikeService.isLikedByMe(mockFeed, currentUserId)).willReturn(false);
    given(ootdService.deleteOotdByFeedId(eq(mockFeed.getId()))).willReturn(
        List.of(mockOotdDto1, mockOotdDto2));
    given(feedMapper.toDto(eq(mockFeed), eq(mockAuthorDto), any(WeatherSummaryDto.class),
        eq(List.of(mockOotdDto1, mockOotdDto2)),
        eq(false))).willReturn(mockFeedDto);

    // when
    FeedDto result = feedService.deleteFeed(feedId, currentUserId);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(mockFeed.getId());
    assertThat(result.getContent()).isEqualTo(mockFeed.getContent());

    // verify
    verify(feedRepository).findById(feedId);
    verify(feedLikeService).isLikedByMe(mockFeed, currentUserId);
    verify(feedLikeService).deleteAllFeedLikeInFeed(mockFeed);
    verify(feedCommentService).deleteFeedCommentsByFeed(mockFeed);
    verify(feedRepository).delete(mockFeed);
    verify(feedMapper).toDto(eq(mockFeed), eq(mockAuthorDto), any(WeatherSummaryDto.class),
        eq(List.of(mockOotdDto1, mockOotdDto2)), eq(false));
  }

  @Test
  @DisplayName("피드를 조회하지 못 해 피드 삭제를 실패한다")
  void deleteFeed_failed_cannot_find_feed() {
    // given
    UUID failedId = UUID.randomUUID();

    given(feedRepository.findById(failedId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> feedService.deleteFeed(failedId, authorId))
        .isInstanceOf(FeedNotFoundException.class);
  }

}