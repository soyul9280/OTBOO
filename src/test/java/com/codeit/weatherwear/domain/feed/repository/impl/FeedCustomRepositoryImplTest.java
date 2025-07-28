package com.codeit.weatherwear.domain.feed.repository.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.codeit.weatherwear.domain.feed.dto.condition.FeedSearchCondition;
import com.codeit.weatherwear.domain.feed.entity.Feed;
import com.codeit.weatherwear.domain.feed.entity.QFeed;
import com.codeit.weatherwear.domain.feed.exception.UnsupportedSortFieldException;
import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.user.entity.Gender;
import com.codeit.weatherwear.domain.user.entity.Role;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.weather.entity.Precipitation;
import com.codeit.weatherwear.domain.weather.entity.PrecipitationsType;
import com.codeit.weatherwear.domain.weather.entity.SkyStatus;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import com.codeit.weatherwear.global.request.SortDirection;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Slice;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class FeedCustomRepositoryImplTest {

  @InjectMocks
  private FeedCustomRepositoryImpl feedCustomRepository;

  @Mock
  private JPAQueryFactory queryFactory;

  private JPAQuery mockQuery;

  private User author;
  private Weather weather;
  private List<Feed> feeds;

  @BeforeEach
  void setUp() {
    mockQuery = mock(JPAQuery.class);

    weather = mock(Weather.class);

    UUID authorId = UUID.randomUUID();
    author = createMockUser(authorId, mock(Location.class));
  }

  @Test
  @DisplayName("현재 존재하는 피드의 전체 개수를 반환한다")
  void getTotalFeedCount_success() {
    // given
    QFeed feed = QFeed.feed;

    given(queryFactory.select(feed.count())).willReturn(mockQuery);
    given(mockQuery.from(feed)).willReturn(mockQuery);
    given(mockQuery.fetchOne()).willReturn(10L);

    // when
    long result = feedCustomRepository.getTotalFeedCount();

    // then
    assertThat(result).isEqualTo(10L);
  }

  @Test
  @DisplayName("쿼리가 Null을 반환할 시 0L을 반환한다")
  void getTotalFeedCount_return_0L_query_return_null() {
    // given
    QFeed feed = QFeed.feed;

    given(queryFactory.select(feed.count())).willReturn(mockQuery);
    given(mockQuery.from(feed)).willReturn(mockQuery);
    given(mockQuery.fetchOne()).willReturn(null);

    // when
    long result = feedCustomRepository.getTotalFeedCount();

    // then
    assertThat(result).isEqualTo(0L);
  }

  @Test
  @DisplayName("생성일을 기준으로 오름차순 정렬이 된 데이터를 반환한다(커서/idAfter 미존재)")
  void searchFeeds_without_cursor_and_idAfter() {
    // given
    int limit = 3;
    createDummyFeedList(limit);
    QFeed feed = QFeed.feed;

    FeedSearchCondition condition = FeedSearchCondition.builder()
        .cursor(null)
        .idAfter(null)
        .limit(limit)
        .sortBy("createdAt")
        .sortDirection(SortDirection.ASCENDING)
        .keywordLike(null)
        .skyStatusEqual(null)
        .precipitationsTypeEqual(null)
        .authorIdEqual(null)
        .build();

    given(queryFactory.selectFrom(feed)).willReturn(mockQuery);
    given(mockQuery.where(any(), any(), any(), any(), any())).willReturn(mockQuery);
    given(mockQuery.orderBy(any(OrderSpecifier[].class))).willReturn(mockQuery);
    given(mockQuery.limit(anyLong())).willReturn(mockQuery);
    given(mockQuery.fetch()).willReturn(feeds); // limit + 1개

    // when
    Slice<Feed> result = feedCustomRepository.searchFeeds(condition);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(limit);
    assertThat(result.hasNext()).isTrue();

    // 생성일 기준 오름차순 정렬 검증
    List<Feed> content = result.getContent();
    for (int i = 0; i < content.size() - 1; i++) {
      assertThat(content.get(i).getCreatedAt())
          .isBeforeOrEqualTo(content.get(i + 1).getCreatedAt());
    }
  }

  @Test
  @DisplayName("생성일을 기준으로 내림차순 정렬이 된 데이터를 반환한다(커서/idAfter 미존재)")
  void searchFeeds_desc_without_cursor_and_idAfter() {
    // given
    int limit = 3;
    createDummyFeedList(limit);
    QFeed feed = QFeed.feed;

    FeedSearchCondition condition = FeedSearchCondition.builder()
        .cursor(null)
        .idAfter(null)
        .limit(limit)
        .sortBy("createdAt")
        .sortDirection(SortDirection.DESCENDING)
        .keywordLike(null)
        .skyStatusEqual(null)
        .precipitationsTypeEqual(null)
        .authorIdEqual(null)
        .build();

    // 생성일 기준 내림차순 정렬
    feeds.sort(Comparator.comparing(Feed::getCreatedAt).reversed());

    given(queryFactory.selectFrom(feed)).willReturn(mockQuery);
    given(mockQuery.where(any(), any(), any(), any(), any())).willReturn(mockQuery);
    given(mockQuery.orderBy(any(OrderSpecifier[].class))).willReturn(mockQuery);
    given(mockQuery.limit(anyLong())).willReturn(mockQuery);
    given(mockQuery.fetch()).willReturn(feeds); // limit + 1개

    // when
    Slice<Feed> result = feedCustomRepository.searchFeeds(condition);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(limit);
    assertThat(result.hasNext()).isTrue();

    // 생성일 기준 내림차순 정렬 검증
    List<Feed> content = result.getContent();
    for (int i = 0; i < content.size() - 1; i++) {
      assertThat(content.get(i).getCreatedAt())
          .isAfterOrEqualTo(content.get(i + 1).getCreatedAt());
    }
  }

  @Test
  @DisplayName("좋아요 개수를 기준으로 내림차순 정렬이 된 데이터를 반환한다")
  void searchFeeds_sort_like_count_desc() {
    // given
    int limit = 3;
    createDummyFeedList(5);
    QFeed feed = QFeed.feed;

    String cursor = Integer.toString(feeds.get(1).getLikeCount());
    UUID idAfter = feeds.get(1).getId();

    // 첫 번째 요소를 조회했다 가정하고 자름
    feeds = feeds.subList(1, 5);
    feeds.sort(Comparator.comparingInt(Feed::getLikeCount).reversed());

    FeedSearchCondition condition = FeedSearchCondition.builder()
        .cursor(cursor)
        .idAfter(idAfter)
        .limit(limit)
        .sortBy("likeCount")
        .sortDirection(SortDirection.DESCENDING)
        .keywordLike(null)
        .skyStatusEqual(null)
        .precipitationsTypeEqual(null)
        .authorIdEqual(null)
        .build();

    given(queryFactory.selectFrom(feed)).willReturn(mockQuery);
    given(mockQuery.where(any(), any(), any(), any(), any())).willReturn(mockQuery);
    given(mockQuery.orderBy(any(OrderSpecifier[].class))).willReturn(mockQuery);
    given(mockQuery.limit(anyLong())).willReturn(mockQuery);
    given(mockQuery.fetch()).willReturn(feeds); // limit + 1개

    // when
    Slice<Feed> result = feedCustomRepository.searchFeeds(condition);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(limit);
    assertThat(result.hasNext()).isTrue();
    assertThat(result.getContent().get(0).getId()).isEqualTo(feeds.get(0).getId());
    assertThat(result.getContent().get(limit - 1).getId()).isEqualTo(
        feeds.get(limit - 1).getId());

    // 좋아요 개수 내림차순 정렬 검증
    List<Feed> content = result.getContent();
    for (int i = 0; i < content.size() - 1; i++) {
      assertThat(content.get(i).getLikeCount())
          .isGreaterThanOrEqualTo(content.get(i + 1).getLikeCount());
    }
  }

  @Test
  @DisplayName("좋아요 개수를 기준으로 오름차순 정렬이 된 데이터를 반환한다")
  void searchFeeds_sort_like_count_asc() {
    // given
    int limit = 3;
    createDummyFeedList(5);
    QFeed feed = QFeed.feed;

    String cursor = Integer.toString(feeds.get(1).getLikeCount());
    UUID idAfter = feeds.get(1).getId();

    // 첫 번째 요소를 조회했다 가정하고 자름
    feeds = feeds.subList(1, 5);
    feeds.sort(Comparator.comparingInt(Feed::getLikeCount));

    FeedSearchCondition condition = FeedSearchCondition.builder()
        .cursor(cursor)
        .idAfter(idAfter)
        .limit(limit)
        .sortBy("likeCount")
        .sortDirection(SortDirection.ASCENDING)
        .keywordLike(null)
        .skyStatusEqual(null)
        .precipitationsTypeEqual(null)
        .authorIdEqual(null)
        .build();

    given(queryFactory.selectFrom(feed)).willReturn(mockQuery);
    given(mockQuery.where(any(), any(), any(), any(), any())).willReturn(mockQuery);
    given(mockQuery.orderBy(any(OrderSpecifier[].class))).willReturn(mockQuery);
    given(mockQuery.limit(anyLong())).willReturn(mockQuery);
    given(mockQuery.fetch()).willReturn(feeds); // limit + 1개

    // when
    Slice<Feed> result = feedCustomRepository.searchFeeds(condition);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(limit);
    assertThat(result.hasNext()).isTrue();
    assertThat(result.getContent().get(0).getId()).isEqualTo(feeds.get(0).getId());
    assertThat(result.getContent().get(limit - 1).getId()).isEqualTo(
        feeds.get(limit - 1).getId());

    // 좋아요 개수 오름차순 정렬 검증
    List<Feed> content = result.getContent();
    for (int i = 0; i < content.size() - 1; i++) {
      assertThat(content.get(i).getLikeCount())
          .isLessThanOrEqualTo(content.get(i + 1).getLikeCount());
    }
  }

  @Test
  @DisplayName("커서와 idAfter가 존재할 때 성공적으로 페이지네이션이 적용된 피드를 반환한다")
  void searchFeeds_with_cursor_and_idAfter() {
    // given
    int limit = 3;
    createDummyFeedList(5);
    QFeed feed = QFeed.feed;

    String cursor = feeds.get(1).getCreatedAt().toString();
    UUID idAfter = feeds.get(1).getId();

    // 첫 번째 요소를 조회했다 가정하고 자름
    feeds = feeds.subList(1, 5);

    FeedSearchCondition condition = FeedSearchCondition.builder()
        .cursor(cursor)
        .idAfter(idAfter)
        .limit(limit)
        .sortBy("createdAt")
        .sortDirection(SortDirection.ASCENDING)
        .keywordLike(null)
        .skyStatusEqual(null)
        .precipitationsTypeEqual(null)
        .authorIdEqual(null)
        .build();

    given(queryFactory.selectFrom(feed)).willReturn(mockQuery);
    given(mockQuery.where(any(), any(), any(), any(), any())).willReturn(mockQuery);
    given(mockQuery.orderBy(any(OrderSpecifier[].class))).willReturn(mockQuery);
    given(mockQuery.limit(anyLong())).willReturn(mockQuery);
    given(mockQuery.fetch()).willReturn(feeds); // limit + 1개

    // when
    Slice<Feed> result = feedCustomRepository.searchFeeds(condition);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(limit);
    assertThat(result.hasNext()).isTrue();
    assertThat(result.getContent().get(0).getId()).isEqualTo(feeds.get(0).getId());
    assertThat(result.getContent().get(limit - 1).getId()).isEqualTo(
        feeds.get(limit - 1).getId());
  }

  @Test
  @DisplayName("커서와 idAfter가 존재할 때 성공적으로 생성일 기준 내림차순으로 정렬된 페이지네이션이 적용된 피드를 반환한다")
  void searchFeeds_desc_with_cursor_and_idAfter() {
    // given
    int limit = 3;
    createDummyFeedList(5);
    QFeed feed = QFeed.feed;

    String cursor = feeds.get(1).getCreatedAt().toString();
    UUID idAfter = feeds.get(1).getId();

    // 첫 번째 요소를 조회했다 가정하고 자름
    feeds = feeds.subList(1, 5);
    feeds.sort(Comparator.comparing(Feed::getCreatedAt).reversed());

    FeedSearchCondition condition = FeedSearchCondition.builder()
        .cursor(cursor)
        .idAfter(idAfter)
        .limit(limit)
        .sortBy("createdAt")
        .sortDirection(SortDirection.DESCENDING)
        .keywordLike(null)
        .skyStatusEqual(null)
        .precipitationsTypeEqual(null)
        .authorIdEqual(null)
        .build();

    given(queryFactory.selectFrom(feed)).willReturn(mockQuery);
    given(mockQuery.where(any(), any(), any(), any(), any())).willReturn(mockQuery);
    given(mockQuery.orderBy(any(OrderSpecifier[].class))).willReturn(mockQuery);
    given(mockQuery.limit(anyLong())).willReturn(mockQuery);
    given(mockQuery.fetch()).willReturn(feeds); // limit + 1개

    // when
    Slice<Feed> result = feedCustomRepository.searchFeeds(condition);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(limit);
    assertThat(result.hasNext()).isTrue();
    assertThat(result.getContent().get(0).getId()).isEqualTo(feeds.get(0).getId());
    assertThat(result.getContent().get(limit - 1).getId()).isEqualTo(
        feeds.get(limit - 1).getId());
  }

  @Test
  @DisplayName("다음 페이지가 없는 페이지네이션이 적용된 피드를 반환한다")
  void searchFeeds_not_has_next_with_cursor_and_idAfter() {
    // given
    int limit = 3;
    createDummyFeedListHasNextFalse(limit);
    QFeed feed = QFeed.feed;

    FeedSearchCondition condition = FeedSearchCondition.builder()
        .cursor(null)
        .idAfter(null)
        .limit(limit)
        .sortBy("createdAt")
        .sortDirection(SortDirection.ASCENDING)
        .keywordLike(null)
        .skyStatusEqual(null)
        .precipitationsTypeEqual(null)
        .authorIdEqual(null)
        .build();

    given(queryFactory.selectFrom(feed)).willReturn(mockQuery);
    given(mockQuery.where(any(), any(), any(), any(), any())).willReturn(mockQuery);
    given(mockQuery.orderBy(any(OrderSpecifier[].class))).willReturn(mockQuery);
    given(mockQuery.limit(anyLong())).willReturn(mockQuery);
    given(mockQuery.fetch()).willReturn(feeds); // limit + 1개

    // when
    Slice<Feed> result = feedCustomRepository.searchFeeds(condition);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(limit);
    assertThat(result.hasNext()).isFalse();
    assertThat(result.getContent().get(0).getId()).isEqualTo(feeds.get(0).getId());
    assertThat(result.getContent().get(limit - 1).getId()).isEqualTo(
        feeds.get(limit - 1).getId());
  }

  @Test
  @DisplayName("잘못된 정렬 기준 입력으로 인하여 예외가 발생한다.")
  void searchFeeds_failed_unsupported_sort_field() {
    // given
    int limit = 3;
    createDummyFeedList(limit);
    QFeed feed = QFeed.feed;

    FeedSearchCondition condition = FeedSearchCondition.builder()
        .cursor(null)
        .idAfter(null)
        .limit(limit)
        .sortBy("failed")
        .sortDirection(SortDirection.ASCENDING)
        .keywordLike(null)
        .skyStatusEqual(null)
        .precipitationsTypeEqual(null)
        .authorIdEqual(null)
        .build();

    given(queryFactory.selectFrom(feed)).willReturn(mockQuery);
    given(mockQuery.where(any(), any(), any(), any(), any())).willReturn(mockQuery);

    // when & then
    assertThatThrownBy(
        () -> feedCustomRepository.searchFeeds(condition)).isInstanceOf(
        UnsupportedSortFieldException.class);
  }

  @Test
  @DisplayName("content 키워드가 포함된 피드만 반환한다")
  void searchFeeds_with_keywordLike() {
    // given
    int limit = 3;
    String keyword = "특별키워드";
    createDummyFeedList(limit + 1);
    QFeed feed = QFeed.feed;

    // 첫 번째 피드만 키워드 포함
    ReflectionTestUtils.setField(feeds.get(0), "content", "이 피드는 " + keyword + "를 포함합니다.");

    // 키워드 포함 피드만 반환하도록 mock 설정
    List<Feed> filtered = feeds.stream()
        .filter(f -> f.getContent().contains(keyword))
        .collect(Collectors.toList());

    FeedSearchCondition condition = FeedSearchCondition.builder()
        .cursor(null)
        .idAfter(null)
        .limit(limit)
        .sortBy("createdAt")
        .sortDirection(SortDirection.ASCENDING)
        .keywordLike(keyword)
        .skyStatusEqual(null)
        .precipitationsTypeEqual(null)
        .authorIdEqual(null)
        .build();

    given(queryFactory.selectFrom(feed)).willReturn(mockQuery);
    given(mockQuery.where(any(), any(), any(), any(), any())).willReturn(mockQuery);
    given(mockQuery.orderBy(any(OrderSpecifier[].class))).willReturn(mockQuery);
    given(mockQuery.limit(anyLong())).willReturn(mockQuery);
    given(mockQuery.fetch()).willReturn(filtered);

    // when
    Slice<Feed> result = feedCustomRepository.searchFeeds(condition);

    // then
    assertThat(result.getContent()).allMatch(f -> f.getContent().contains(keyword));
  }

  @Test
  @DisplayName("특정 하늘 상태의 피드만 반환한다")
  void searchFeeds_with_skyStatus() {
    // given
    when(weather.getSkyStatus()).thenReturn(SkyStatus.CLEAR);

    Weather cloudyWeather = mock(Weather.class);
    when(cloudyWeather.getSkyStatus()).thenReturn(SkyStatus.CLOUDY);

    int limit = 3;
    createDummyFeedList(limit + 1);
    QFeed feed = QFeed.feed;

    // 첫 번째 피드만 키워드 포함
    ReflectionTestUtils.setField(feeds.get(0), "weather", cloudyWeather);

    // 키워드 포함 피드만 반환하도록 mock 설정
    List<Feed> filtered = feeds.stream()
        .filter(f -> f.getWeather().getSkyStatus().equals(SkyStatus.CLOUDY))
        .collect(Collectors.toList());

    FeedSearchCondition condition = FeedSearchCondition.builder()
        .cursor(null)
        .idAfter(null)
        .limit(limit)
        .sortBy("createdAt")
        .sortDirection(SortDirection.ASCENDING)
        .keywordLike(null)
        .skyStatusEqual(SkyStatus.CLOUDY)
        .precipitationsTypeEqual(null)
        .authorIdEqual(null)
        .build();

    given(queryFactory.selectFrom(feed)).willReturn(mockQuery);
    given(mockQuery.where(any(), any(), any(), any(), any())).willReturn(mockQuery);
    given(mockQuery.orderBy(any(OrderSpecifier[].class))).willReturn(mockQuery);
    given(mockQuery.limit(anyLong())).willReturn(mockQuery);
    given(mockQuery.fetch()).willReturn(filtered);

    // when
    Slice<Feed> result = feedCustomRepository.searchFeeds(condition);

    // then
    assertThat(result.getContent().size()).isEqualTo(1);
    assertThat(result.getContent()).allMatch(
        f -> f.getWeather().getSkyStatus().equals(SkyStatus.CLOUDY));
  }

  @Test
  @DisplayName("특정 강우 상태의 피드만 반환한다")
  void searchFeeds_with_PrecipitationType() {
    // given
    Precipitation precipitation = mock(Precipitation.class);
    when(precipitation.getType()).thenReturn(PrecipitationsType.NONE);
    when(weather.getPrecipitation()).thenReturn(precipitation);

    Weather cloudyWeather = mock(Weather.class);
    Precipitation cloudyPrecipitation = mock(Precipitation.class);
    when(cloudyPrecipitation.getType()).thenReturn(PrecipitationsType.RAIN);
    when(cloudyWeather.getPrecipitation()).thenReturn(cloudyPrecipitation);

    int limit = 3;
    createDummyFeedList(limit + 1);
    QFeed feed = QFeed.feed;

    // 첫 번째 피드만 키워드 포함
    ReflectionTestUtils.setField(feeds.get(1), "weather", cloudyWeather);

    // 키워드 포함 피드만 반환하도록 mock 설정
    List<Feed> filtered = feeds.stream()
        .filter(f -> f.getWeather().getPrecipitation().getType().equals(PrecipitationsType.RAIN))
        .collect(Collectors.toList());

    FeedSearchCondition condition = FeedSearchCondition.builder()
        .cursor(null)
        .idAfter(null)
        .limit(limit)
        .sortBy("createdAt")
        .sortDirection(SortDirection.ASCENDING)
        .keywordLike(null)
        .skyStatusEqual(null)
        .precipitationsTypeEqual(PrecipitationsType.RAIN)
        .authorIdEqual(null)
        .build();

    given(queryFactory.selectFrom(feed)).willReturn(mockQuery);
    given(mockQuery.where(any(), any(), any(), any(), any())).willReturn(mockQuery);
    given(mockQuery.orderBy(any(OrderSpecifier[].class))).willReturn(mockQuery);
    given(mockQuery.limit(anyLong())).willReturn(mockQuery);
    given(mockQuery.fetch()).willReturn(filtered);

    // when
    Slice<Feed> result = feedCustomRepository.searchFeeds(condition);

    // then
    assertThat(result.getContent().size()).isEqualTo(1);
    assertThat(result.getContent()).allMatch(
        f -> f.getWeather().getPrecipitation().getType().equals(PrecipitationsType.RAIN));
  }

  @Test
  @DisplayName("특정 작가의 피드만 반환한다")
  void searchFeeds_with_author_id() {
    // given
    UUID anotherId = UUID.randomUUID();
    User anotherAuthor = mock(User.class);
    when(anotherAuthor.getId()).thenReturn(anotherId);

    int limit = 3;
    createDummyFeedList(limit + 1);
    QFeed feed = QFeed.feed;

    // 첫 번째 피드만 키워드 포함
    ReflectionTestUtils.setField(feeds.get(1), "author", anotherAuthor);
    ReflectionTestUtils.setField(feeds.get(2), "author", anotherAuthor);

    // 키워드 포함 피드만 반환하도록 mock 설정
    List<Feed> filtered = feeds.stream()
        .filter(f -> f.getAuthor().getId().equals(anotherId))
        .collect(Collectors.toList());

    FeedSearchCondition condition = FeedSearchCondition.builder()
        .cursor(null)
        .idAfter(null)
        .limit(limit)
        .sortBy("createdAt")
        .sortDirection(SortDirection.ASCENDING)
        .keywordLike(null)
        .skyStatusEqual(null)
        .precipitationsTypeEqual(null)
        .authorIdEqual(anotherId)
        .build();

    given(queryFactory.selectFrom(feed)).willReturn(mockQuery);
    given(mockQuery.where(any(), any(), any(), any(), any())).willReturn(mockQuery);
    given(mockQuery.orderBy(any(OrderSpecifier[].class))).willReturn(mockQuery);
    given(mockQuery.limit(anyLong())).willReturn(mockQuery);
    given(mockQuery.fetch()).willReturn(filtered);

    // when
    Slice<Feed> result = feedCustomRepository.searchFeeds(condition);

    // then
    assertThat(result.getContent().size()).isEqualTo(2);
    assertThat(result.getContent()).allMatch(
        f -> f.getAuthor().getId().equals(anotherId));
  }

  // 생성용 Private 메서드 ---------------

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

  private void createDummyFeedList(int limit) {
    // Feed 더미 리스트 생성 (limit + 1개)
    List<Feed> feedList = new ArrayList<>();
    for (int i = 0; i < limit + 1; i++) {
      Feed feed = Feed.builder()
          .author(author)
          .content("Feed " + i)
          .weather(weather)
          .commentCount(ThreadLocalRandom.current().nextInt(0, 100))
          .likeCount(ThreadLocalRandom.current().nextInt(0, 100))
          .build();
      ReflectionTestUtils.setField(feed, "id", UUID.randomUUID());
      ReflectionTestUtils.setField(feed, "createdAt", Instant.now().plusSeconds(i));
      feedList.add(feed);
    }
    this.feeds = feedList;
  }

  private void createDummyFeedListHasNextFalse(int limit) {
    // Feed 더미 리스트 생성 (limit)
    List<Feed> feedList = new ArrayList<>();
    for (int i = 0; i < limit; i++) {
      Feed feed = Feed.builder()
          .author(author)
          .content("Feed " + i)
          .weather(weather)
          .commentCount(ThreadLocalRandom.current().nextInt(0, 100))
          .likeCount(ThreadLocalRandom.current().nextInt(0, 100))
          .build();
      ReflectionTestUtils.setField(feed, "id", UUID.randomUUID());
      ReflectionTestUtils.setField(feed, "createdAt", Instant.now().plusSeconds(i));
      feedList.add(feed);
    }
    this.feeds = feedList;
  }

}