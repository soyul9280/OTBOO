package com.codeit.weatherwear.domain.feed.repository.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.codeit.weatherwear.domain.feed.dto.condition.FeedCommentSearchCondition;
import com.codeit.weatherwear.domain.feed.entity.Feed;
import com.codeit.weatherwear.domain.feed.entity.FeedComment;
import com.codeit.weatherwear.domain.feed.entity.QFeedComment;
import com.codeit.weatherwear.domain.feed.exception.UnsupportedSortFieldException;
import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.user.entity.Gender;
import com.codeit.weatherwear.domain.user.entity.QUser;
import com.codeit.weatherwear.domain.user.entity.Role;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import com.codeit.weatherwear.global.request.SortDirection;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
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
class FeedCommentCustomRepositoryImplTest {

  @InjectMocks
  private FeedCommentCustomRepositoryImpl feedCommentCustomRepository;

  @Mock
  private JPAQueryFactory queryFactory;

  private Feed feed;
  private User author;
  private List<FeedComment> feedComments;

  private JPAQuery mockQuery;

  @BeforeEach
  void setUp() {
    // QueryDSL JPAQuery mock 체이닝 ->
    // Java의 제네릭 타입 소거로 인한 명시적 캐스팅 (해도 안해도 Unchecked assignment 발생)
    mockQuery = mock(JPAQuery.class);

    // Feed, User 더미 생성
    UUID userId = UUID.randomUUID();
    User author = createMockUser(userId, mock(Location.class));

    UUID feedId = UUID.randomUUID();
    Feed feed = createMockFeed(feedId, author, mock(Weather.class), "content");

    // 테스트에서 사용하도록 필드에 저장
    this.feed = feed;
    this.author = author;
  }

  @Test
  @DisplayName("커서와 idAfter가 존재할 때 성공적으로 페이지네이션이 적용된 피드 댓글을 반환한다")
  void searchFeedComments_success_with_cursor_and_idAfter() {
    // given
    int limit = 3;
    createDummyCommentList(limit);

    UUID feedId = feed.getId();
    UUID idAfter = feedComments.get(0).getId();
    String cursor = feedComments.get(0).getCreatedAt().toString();

    FeedCommentSearchCondition condition = FeedCommentSearchCondition.builder()
        .feedId(feedId)
        .cursor(cursor)
        .idAfter(idAfter)
        .limit(limit)
        .sortBy("createdAt")
        .sortDirection(SortDirection.ASCENDING)
        .build();

    given(queryFactory.selectFrom(QFeedComment.feedComment)).willReturn(mockQuery);
    given(mockQuery.join(QFeedComment.feedComment.author, QUser.user)).willReturn(mockQuery);
    given(mockQuery.fetchJoin()).willReturn(mockQuery);
    given(mockQuery.where(any(), any())).willReturn(mockQuery);
    given(mockQuery.orderBy(any(OrderSpecifier[].class))).willReturn(mockQuery);
    given(mockQuery.limit(anyLong())).willReturn(mockQuery);
    given(mockQuery.fetch()).willReturn(feedComments); // limit + 1개

    // when
    Slice<FeedComment> result = feedCommentCustomRepository.searchFeedComments(condition);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(limit);
    assertThat(result.getContent().get(limit - 1).getId()).isEqualTo(
        feedComments.get(limit - 1).getId());
    assertThat(result.hasNext()).isTrue();
  }

  @Test
  @DisplayName("커서와 idAfter가 존재할 때 성공적으로 페이지네이션이 적용된 피드 댓글을 내림차순으로 반환한다")
  void searchFeedComments_success_desc_with_cursor_and_idAfter() {
    // given
    int limit = 3;
    createDummyCommentList(limit);

    UUID feedId = feed.getId();
    UUID idAfter = feedComments.get(0).getId();
    String cursor = feedComments.get(0).getCreatedAt().toString();

    FeedCommentSearchCondition condition = FeedCommentSearchCondition.builder()
        .feedId(feedId)
        .cursor(cursor)
        .idAfter(idAfter)
        .limit(limit)
        .sortBy("createdAt")
        .sortDirection(SortDirection.DESCENDING)
        .build();

    given(queryFactory.selectFrom(QFeedComment.feedComment)).willReturn(mockQuery);
    given(mockQuery.join(QFeedComment.feedComment.author, QUser.user)).willReturn(mockQuery);
    given(mockQuery.fetchJoin()).willReturn(mockQuery);
    given(mockQuery.where(any(), any())).willReturn(mockQuery);
    given(mockQuery.orderBy(any(OrderSpecifier[].class))).willReturn(mockQuery);
    given(mockQuery.limit(anyLong())).willReturn(mockQuery);
    given(mockQuery.fetch()).willReturn(feedComments); // limit + 1개

    // when
    Slice<FeedComment> result = feedCommentCustomRepository.searchFeedComments(condition);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(limit);
    assertThat(result.getContent().get(0).getId()).isEqualTo(feedComments.get(0).getId());
    assertThat(result.getContent().get(limit - 1).getId()).isEqualTo(
        feedComments.get(limit - 1).getId());
    assertThat(result.hasNext()).isTrue();
  }

  @Test
  @DisplayName("커서와 idAfter가 존재하지 않을 때 성공적으로 페이지네이션이 적용된 피드 댓글을 반환한다")
  void searchFeedComments_success_without_cursor_and_idAfter() {
    // given
    int limit = 2;
    createDummyCommentList(limit);

    UUID feedId = feed.getId();

    FeedCommentSearchCondition condition = FeedCommentSearchCondition.builder()
        .feedId(feedId)
        .cursor(null)
        .idAfter(null)
        .limit(limit)
        .sortBy("createdAt")
        .sortDirection(SortDirection.DESCENDING)
        .build();

    given(queryFactory.selectFrom(QFeedComment.feedComment)).willReturn(mockQuery);
    given(mockQuery.join(QFeedComment.feedComment.author, QUser.user)).willReturn(mockQuery);
    given(mockQuery.fetchJoin()).willReturn(mockQuery);
    given(mockQuery.where(any(), any())).willReturn(mockQuery);
    given(mockQuery.orderBy(any(OrderSpecifier[].class))).willReturn(mockQuery);
    given(mockQuery.limit(anyLong())).willReturn(mockQuery);
    given(mockQuery.fetch()).willReturn(feedComments); // limit + 1개

    // when
    Slice<FeedComment> result = feedCommentCustomRepository.searchFeedComments(condition);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(limit);
    assertThat(result.getContent().get(limit - 1).getId()).isEqualTo(
        feedComments.get(limit - 1).getId());
    assertThat(result.hasNext()).isTrue();
  }

  @Test
  @DisplayName("feedId가 null이면 빈 리스트를 반환한다.")
  void searchFeedComments_return_empty_list_cause_feed_id_different() {
    // given
    int limit = 2;
    createDummyCommentList(limit);

    UUID feedId = null;

    FeedCommentSearchCondition condition = FeedCommentSearchCondition.builder()
        .feedId(feedId)
        .cursor(null)
        .idAfter(null)
        .limit(limit)
        .sortBy("createdAt")
        .sortDirection(SortDirection.DESCENDING)
        .build();

    given(queryFactory.selectFrom(QFeedComment.feedComment)).willReturn(mockQuery);
    given(mockQuery.join(QFeedComment.feedComment.author, QUser.user)).willReturn(mockQuery);
    given(mockQuery.fetchJoin()).willReturn(mockQuery);
    given(mockQuery.where(any(), any())).willReturn(mockQuery);
    given(mockQuery.orderBy(any(OrderSpecifier[].class))).willReturn(mockQuery);
    given(mockQuery.limit(anyLong())).willReturn(mockQuery);
    given(mockQuery.fetch()).willReturn(Collections.EMPTY_LIST); // limit + 1개

    // when
    Slice<FeedComment> result = feedCommentCustomRepository.searchFeedComments(condition);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isEmpty();
    assertThat(result.hasNext()).isFalse();
  }

  @Test
  @DisplayName("hasNext가 없는 페이지네이션 데이터를 반환한다.")
  void searchFeedComments_success_without_hasNext() {
    // given
    int limit = 3;
    createDummyCommentListHasNextFalse(limit);

    UUID feedId = feed.getId();

    FeedCommentSearchCondition condition = FeedCommentSearchCondition.builder()
        .feedId(feedId)
        .cursor(null)
        .idAfter(null)
        .limit(limit)
        .sortBy("createdAt")
        .sortDirection(SortDirection.DESCENDING)
        .build();

    given(queryFactory.selectFrom(QFeedComment.feedComment)).willReturn(mockQuery);
    given(mockQuery.join(QFeedComment.feedComment.author, QUser.user)).willReturn(mockQuery);
    given(mockQuery.fetchJoin()).willReturn(mockQuery);
    given(mockQuery.where(any(), any())).willReturn(mockQuery);
    given(mockQuery.orderBy(any(OrderSpecifier[].class))).willReturn(mockQuery);
    given(mockQuery.limit(anyLong())).willReturn(mockQuery);
    given(mockQuery.fetch()).willReturn(feedComments);

    // when
    Slice<FeedComment> result = feedCommentCustomRepository.searchFeedComments(condition);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(limit);
    assertThat(result.getContent().get(limit - 1).getId()).isEqualTo(
        feedComments.get(limit - 1).getId());
    assertThat(result.hasNext()).isFalse();
  }

  @Test
  @DisplayName("잘못된 sortBy 입력으로 인하여 예외가 발생한다")
  void searchFeedComments_failed_incorrect_sort_field() {
    // given
    int limit = 3;
    createDummyCommentListHasNextFalse(limit);

    UUID feedId = feed.getId();

    FeedCommentSearchCondition condition = FeedCommentSearchCondition.builder()
        .feedId(feedId)
        .cursor(null)
        .idAfter(null)
        .limit(limit)
        .sortBy("failed")
        .sortDirection(SortDirection.DESCENDING)
        .build();

    given(queryFactory.selectFrom(QFeedComment.feedComment)).willReturn(mockQuery);
    given(mockQuery.join(QFeedComment.feedComment.author, QUser.user)).willReturn(mockQuery);
    given(mockQuery.fetchJoin()).willReturn(mockQuery);
    given(mockQuery.where(any(), any())).willReturn(mockQuery);

    // when & then
    assertThatThrownBy(
        () -> feedCommentCustomRepository.searchFeedComments(condition)).isInstanceOf(
        UnsupportedSortFieldException.class);
  }

  @Test
  @DisplayName("피드 ID에 해당하는 댓글 수를 정상적으로 반환한다")
  void getTotalFeedCommentCount_success() {
    // given
    UUID feedId = feed.getId();
    QFeedComment feedComment = QFeedComment.feedComment;

    given(queryFactory.select(feedComment.count())).willReturn(mockQuery);
    given(mockQuery.from(feedComment)).willReturn(mockQuery);
    given(mockQuery.where(feedComment.feed.id.eq(feedId))).willReturn(mockQuery);
    given(mockQuery.fetchOne()).willReturn(5L);

    // when
    Long result = feedCommentCustomRepository.getTotalFeedCommentCount(feedId);

    // then
    assertThat(result).isEqualTo(5L);
  }

  @Test
  @DisplayName("피드 ID에 해당하는 댓글 수가 Null이면 0L을 반환한다.")
  void getTotalFeedCommentCount_comment_is_null() {
    // given
    UUID feedId = feed.getId();
    QFeedComment feedComment = QFeedComment.feedComment;

    given(queryFactory.select(feedComment.count())).willReturn(mockQuery);
    given(mockQuery.from(feedComment)).willReturn(mockQuery);
    given(mockQuery.where(feedComment.feed.id.eq(feedId))).willReturn(mockQuery);
    given(mockQuery.fetchOne()).willReturn(null);

    // when
    Long result = feedCommentCustomRepository.getTotalFeedCommentCount(feedId);

    // then
    assertThat(result).isEqualTo(0L);
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


  private Feed createMockFeed(UUID feedId, User author, Weather weather, String content) {
    Feed feed = Feed.builder()
        .author(author)
        .content(content)
        .weather(weather)
        .commentCount(0)
        .likeCount(0)
        .build();
    ReflectionTestUtils.setField(feed, "id", feedId);
    ReflectionTestUtils.setField(feed, "createdAt", Instant.now());
    ReflectionTestUtils.setField(feed, "updatedAt", Instant.now());
    return feed;
  }

  private void createDummyCommentList(int limit) {
    // FeedComment 더미 리스트 생성 (limit + 1개)
    List<FeedComment> commentList = new ArrayList<>();
    for (int i = 0; i < limit + 1; i++) {
      FeedComment comment = FeedComment.builder()
          .feed(feed)
          .author(author)
          .content("댓글 내용 " + i)
          .build();
      ReflectionTestUtils.setField(comment, "id", UUID.randomUUID());
      ReflectionTestUtils.setField(comment, "createdAt", Instant.now().plusSeconds(i));
      commentList.add(comment);
    }
    this.feedComments = commentList;
  }

  private void createDummyCommentListHasNextFalse(int limit) {
    // FeedComment 더미 리스트 생성 (limit)
    List<FeedComment> commentList = new ArrayList<>();
    for (int i = 0; i < limit; i++) {
      FeedComment comment = FeedComment.builder()
          .feed(feed)
          .author(author)
          .content("댓글 내용 " + i)
          .build();
      ReflectionTestUtils.setField(comment, "id", UUID.randomUUID());
      ReflectionTestUtils.setField(comment, "createdAt", Instant.now().plusSeconds(i));
      commentList.add(comment);
    }
    this.feedComments = commentList;
  }

}