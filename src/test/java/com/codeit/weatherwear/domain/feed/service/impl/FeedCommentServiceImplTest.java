package com.codeit.weatherwear.domain.feed.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.codeit.weatherwear.domain.feed.dto.condition.FeedCommentSearchCondition;
import com.codeit.weatherwear.domain.feed.dto.request.FeedCommentCreateRequest;
import com.codeit.weatherwear.domain.feed.dto.request.FeedCommentGetParamRequest;
import com.codeit.weatherwear.domain.feed.dto.response.FeedCommentDto;
import com.codeit.weatherwear.domain.feed.entity.Feed;
import com.codeit.weatherwear.domain.feed.entity.FeedComment;
import com.codeit.weatherwear.domain.feed.exception.FeedNotFoundException;
import com.codeit.weatherwear.domain.feed.mapper.FeedCommentMapper;
import com.codeit.weatherwear.domain.feed.repository.FeedCommentRepository;
import com.codeit.weatherwear.domain.feed.repository.FeedRepository;
import com.codeit.weatherwear.domain.follow.dto.UserSummaryDto;
import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.user.entity.Gender;
import com.codeit.weatherwear.domain.user.entity.Role;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.exception.UserNotFoundException;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import com.codeit.weatherwear.global.request.SortDirection;
import com.codeit.weatherwear.global.response.PageResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class FeedCommentServiceImplTest {

  @InjectMocks
  private FeedCommentServiceImpl feedCommentService;

  @Mock
  private FeedCommentRepository feedCommentRepository;
  @Mock
  private FeedRepository feedRepository;
  @Mock
  private UserRepository userRepository;

  @Mock
  private FeedCommentMapper feedCommentMapper;

  private FeedCommentCreateRequest createRequest;
  private FeedCommentGetParamRequest getParamRequest;
  private FeedCommentSearchCondition condition;

  private Location mockLocation;
  private UUID authorId;
  private User author;
  private UserSummaryDto authorDto;

  private UUID feedId;
  private Feed feed;
  private long commentTotalCount;

  private UUID commentId1, commentId2;
  private Instant commentCreatedAt1, commentCreatedAt2;
  private FeedComment comment1, comment2;
  private String commentContent1, commentContent2;
  private FeedCommentDto commentDto1, commentDto2;
  private List<FeedComment> feedCommentList;
  private Slice<FeedComment> commentSlice;

  @BeforeEach
  void setUp() {
    mockLocation = mock(Location.class);

    authorId = UUID.randomUUID();
    feedId = UUID.randomUUID();

    author = User.builder()
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

    authorDto = UserSummaryDto.from(author);

    feed = Feed.builder()
        .author(author)
        .content("feed1")
        .commentCount(0)
        .likeCount(0)
        .build();
    ReflectionTestUtils.setField(feed, "id", feedId);

    commentId1 = UUID.randomUUID();
    commentContent1 = "comment1";
    commentCreatedAt1 = Instant.now().minusSeconds(60);

    commentId2 = UUID.randomUUID();
    commentContent2 = "comment2";
    commentCreatedAt2 = Instant.now().minusSeconds(30);

    comment1 = FeedComment.builder()
        .feed(feed)
        .author(author)
        .content(commentContent1)
        .build();
    ReflectionTestUtils.setField(comment1, "id", commentId1);
    ReflectionTestUtils.setField(comment1, "createdAt", commentCreatedAt1);

    comment2 = FeedComment.builder()
        .feed(feed)
        .author(author)
        .content(commentContent2)
        .build();
    ReflectionTestUtils.setField(comment2, "id", commentId2);
    ReflectionTestUtils.setField(comment2, "createdAt", commentCreatedAt2);

    commentDto1 = FeedCommentDto.builder()
        .id(commentId1)
        .feedId(feedId)
        .author(authorDto)
        .content(commentContent1)
        .createdAt(Instant.now())
        .build();
    commentDto2 = FeedCommentDto.builder()
        .id(commentId2)
        .feedId(feedId)
        .author(authorDto)
        .content(commentContent2)
        .createdAt(Instant.now())
        .build();
  }

  private FeedCommentCreateRequest createRequest(UUID authorId, UUID feedId, String content) {
    return FeedCommentCreateRequest.builder()
        .authorId(authorId)
        .feedId(feedId)
        .content(content)
        .build();
  }

  private FeedCommentGetParamRequest createParamRequest(String cursor, UUID idAfter, int limit) {
    return FeedCommentGetParamRequest.builder()
        .feedId(feedId)
        .cursor(cursor)
        .idAfter(idAfter)
        .limit(limit)
        .build();
  }

  private FeedCommentSearchCondition createCondition(FeedCommentGetParamRequest request) {
    return FeedCommentSearchCondition.builder()
        .feedId(request.getFeedId())
        .cursor(request.getCursor())
        .idAfter(request.getIdAfter())
        .limit(request.getLimit())
        .sortBy("createdAt")
        .sortDirection(SortDirection.DESCENDING)
        .build();
  }

  @Test
  @DisplayName("Feed에 댓글을 성공적으로 작성한다")
  void createFeedComment_success() {
    // given
    FeedCommentCreateRequest request = createRequest(authorId, feedId, commentContent1);
    given(feedRepository.findById(feedId)).willReturn(Optional.of(feed));
    given(userRepository.findById(authorId)).willReturn(Optional.of(author));
    given(feedCommentMapper.toEntity(feed, author, request.getContent())).willReturn(comment1);
    given(feedCommentRepository.save(comment1)).willReturn(comment1);
    given(feedCommentMapper.toDto(comment1, authorDto)).willReturn(commentDto1);

    // when
    FeedCommentDto result = feedCommentService.createFeedComment(feedId, request);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(commentId1);
    assertThat(result.getFeedId()).isEqualTo(feedId);
    assertThat(result.getAuthor().userId()).isEqualTo(authorId);
  }

  @Test
  @DisplayName("사용자를 찾지 못해 댓글 생성에 실패합니다.")
  void createComment_failed_cannot_find_feed() {
    // given
    UUID failedFeedId = UUID.randomUUID();
    FeedCommentCreateRequest failedRequest = createRequest(authorId, failedFeedId, commentContent1);

    given(feedRepository.findById(failedFeedId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> feedCommentService.createFeedComment(failedFeedId, failedRequest))
        .isInstanceOf(FeedNotFoundException.class);
  }

  @Test
  @DisplayName("사용자를 찾지 못해 댓글 생성에 실패한다")
  void createComment_failed_cannot_find_user() {
    // given
    UUID failedAuthorId = UUID.randomUUID();
    FeedCommentCreateRequest failedRequest = createRequest(failedAuthorId, feedId, commentContent1);

    given(feedRepository.findById(feedId)).willReturn(Optional.of(feed));
    given(userRepository.findById(failedAuthorId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> feedCommentService.createFeedComment(feedId, failedRequest))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  @DisplayName("특정 피드의 모든 댓글을 삭제한다")
  void deleteFeedCommentsByFeed_success() {
    // given
    feedCommentList = List.of(comment1, comment2);
    given(feedCommentRepository.findAllByFeed(feed)).willReturn(feedCommentList);

    // when
    feedCommentService.deleteFeedCommentsByFeed(feed);

    // then
    then(feedCommentRepository).should(times(1)).findAllByFeed(feed);
    then(feedCommentRepository).should(times(1)).deleteAll(feedCommentList);
  }

  @Test
  @DisplayName("페이지네이션이 적용된 특정 피드의 댓글 리스트를 성공적으로 불러온다")
  void getFeedList_success() {
    // given
    feedCommentList = List.of(comment1, comment2);
    commentTotalCount = feedCommentList.size();
    FeedCommentGetParamRequest paramRequest = createParamRequest(null, null,
        feedCommentList.size());
    FeedCommentSearchCondition condition = createCondition(paramRequest);
    commentSlice = new SliceImpl<>(feedCommentList, PageRequest.of(0, feedCommentList.size()),
        false);

    given(userRepository.findAllById(Set.of(authorId))).willReturn(List.of(author));
    given(
        feedCommentRepository.searchFeedComments(any(FeedCommentSearchCondition.class))).willReturn(
        commentSlice);
    given(feedCommentRepository.getTotalFeedCommentCount(feedId)).willReturn(commentTotalCount);
    given(feedCommentMapper.toDto(comment1, authorDto)).willReturn(commentDto1);
    given(feedCommentMapper.toDto(comment2, authorDto)).willReturn(commentDto2);

    // when
    PageResponse<FeedCommentDto> resultList = feedCommentService.getFeedComments(feedId,
        paramRequest);

    // then
    assertThat(resultList).isNotNull();
    assertThat(resultList.data()).doesNotContainNull();
    assertThat(resultList.data()).hasSize(2);
    assertThat(resultList.data())
        .extracting(FeedCommentDto::getId)
        .containsExactly(commentId1, commentId2);
    assertThat(resultList.hasNext()).isFalse();
    assertThat(resultList.sortBy()).isEqualTo(condition.getSortBy());
    assertThat(resultList.sortDirection()).isEqualTo(condition.getSortDirection().name());

    // verify
    verify(feedCommentRepository).searchFeedComments(any(FeedCommentSearchCondition.class));
    verify(feedCommentRepository).getTotalFeedCommentCount(feedId);
    verify(feedCommentMapper).toDto(comment1, authorDto);
    verify(feedCommentMapper).toDto(comment2, authorDto);
  }

}