package com.codeit.weatherwear.domain.feed.repository.impl;

import static org.mockito.Mockito.mock;

import com.codeit.weatherwear.domain.feed.dto.condition.FeedCommentSearchCondition;
import com.codeit.weatherwear.domain.feed.entity.Feed;
import com.codeit.weatherwear.domain.feed.entity.FeedComment;
import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.user.entity.Gender;
import com.codeit.weatherwear.domain.user.entity.Role;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import com.codeit.weatherwear.global.request.SortDirection;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

  @BeforeEach
  void setUp() {
    // Feed, User 더미 생성
    UUID userId = UUID.randomUUID();
    User author = createMockUser(userId, mock(Location.class));

    UUID feedId = UUID.randomUUID();
    Feed feed = createMockFeed(feedId, author, mock(Weather.class), "content");

    // FeedComment 더미 리스트 생성 (limit + 1개)
    int limit = 3;
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

    // 테스트에서 사용하도록 필드에 저장
    this.feed = feed;
    this.author = author;
    this.feedComments = commentList;
  }

  @Test
  @DisplayName("성공적으로 페이지네이션이 적용된 피드 댓글을 반환한다")
  void searchFeedComments_success() {
    // given
    int limit = 3;
    UUID feedId = feed.getId();
    UUID idAfter = feedComments.get(0).getId();
    String cursor = feedComments.get(0).getCreatedAt().toString();

    FeedCommentSearchCondition.builder()
        .feedId(feedId)
        .cursor(cursor)
        .idAfter(idAfter)
        .limit(limit)
        .sortBy("createdAt")
        .sortDirection(SortDirection.ASCENDING)
        .build();

    // QueryDSL JPAQuery mock 체이닝 ->
    // Java의 제네릭 타입 소거로 인한 명시적 캐스팅 (해도 안해도 Unchecked assignment 발생)
    JPAQuery<FeedComment> mockQuery = (JPAQuery<FeedComment>) mock(JPAQuery.class);

//    given(queryFactory.selectFrom(any(QFeedComment.class))).willReturn(mockQuery);
//    given(mockQuery.join((any(), any())).willReturn(mockQuery);
//    given(mockQuery.fetchJoin()).willReturn(mockQuery);
//    given(mockQuery.where(any(), any())).willReturn(mockQuery);
//    given(mockQuery.orderBy(any(OrderSpecifier[].class))).willReturn(mockQuery);
//    given(mockQuery.limit(anyLong())).willReturn(mockQuery);
//    given(mockQuery.fetch()).willReturn(feedComments);

    // when

    // then
  }

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

}