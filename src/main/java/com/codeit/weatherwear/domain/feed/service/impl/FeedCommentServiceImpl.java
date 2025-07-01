package com.codeit.weatherwear.domain.feed.service.impl;

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
import com.codeit.weatherwear.domain.feed.service.FeedCommentService;
import com.codeit.weatherwear.domain.follow.dto.UserSummaryDto;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.exception.UserNotFoundException;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import com.codeit.weatherwear.global.response.PageResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedCommentServiceImpl implements FeedCommentService {

  private final FeedCommentRepository feedCommentRepository;
  private final FeedRepository feedRepository;
  private final UserRepository userRepository;

  private final FeedCommentMapper feedCommentMapper;

  @Transactional
  @Override
  public FeedCommentDto createFeedComment(UUID feedId, FeedCommentCreateRequest request) {
    log.debug("Request Create Feed Comment - Feed: {}", feedId);
    Feed feed = feedRepository.findById(feedId)
        .orElseThrow(() -> new FeedNotFoundException(feedId));
    User author = userRepository.findById(request.getAuthorId())
        .orElseThrow(UserNotFoundException::new);

    feed.increaseCommentCount();
    FeedComment comment = feedCommentMapper.toEntity(feed, author, request.getContent());
    FeedComment saved = feedCommentRepository.save(comment);

    UserSummaryDto userSummary = UserSummaryDto.from(saved.getAuthor());
    FeedCommentDto feedCommentDto = feedCommentMapper.toDto(saved, userSummary);
    log.info("Create feed comment complete: {}", feedCommentDto.getId());
    return feedCommentDto;
  }

  @Transactional(readOnly = true)
  @Override
  public PageResponse<FeedCommentDto> getFeedComments(UUID feedId,
      FeedCommentGetParamRequest queryRequest) {
    log.debug("Request Get Feed Comments - Feed: {}", feedId);
    FeedCommentSearchCondition condition = queryRequest.toSearchCondition();
    Slice<FeedComment> feedCommentSlice = feedCommentRepository.searchFeedComments(condition);
    long totalCount = feedCommentRepository.getTotalFeedCommentCount(feedId);

    return toPageResponse(feedCommentSlice, condition, totalCount);
  }

  @Transactional
  @Override
  public void deleteFeedCommentsByFeed(Feed feed) {
    log.debug("Request Feed's all comment delete: {}", feed.getId());
    List<FeedComment> feedCommentList = feedCommentRepository.findAllByFeed(feed);
    feedCommentRepository.deleteAll(feedCommentList);
    log.info("Delete all feed comments({}) in feed: {}", feedCommentList.size(), feed.getId());
  }

  private PageResponse<FeedCommentDto> toPageResponse(Slice<FeedComment> commentSlice,
      FeedCommentSearchCondition condition, long totalCount) {

    List<FeedComment> comments = commentSlice.getContent();

    Map<UUID, UserSummaryDto> userSummaryMap = userRepository.findAllById(
            comments.stream()
                .map(comment -> comment.getAuthor().getId())
                .collect(Collectors.toSet())
        ).stream()
        .collect(Collectors.toMap(User::getId, UserSummaryDto::from));

    List<FeedCommentDto> data = comments.stream()
        .map(comment -> {
          UserSummaryDto userSummary = userSummaryMap.get(comment.getAuthor().getId());
          return feedCommentMapper.toDto(comment, userSummary);
        })
        .toList();

    UUID nextIdAfter = null;
    Object nextCursor = null;
    if (commentSlice.hasNext() && !data.isEmpty()) {
      FeedComment nextFirstItem = comments.get(comments.size() - 1);
      nextIdAfter = nextFirstItem.getId();
      nextCursor = nextFirstItem.getCreatedAt();
    }

    return new PageResponse<>(
        data,
        nextCursor,
        nextIdAfter,
        commentSlice.hasNext(),
        totalCount,
        condition.getSortBy(),
        condition.getSortDirection().name()
    );
  }

}
