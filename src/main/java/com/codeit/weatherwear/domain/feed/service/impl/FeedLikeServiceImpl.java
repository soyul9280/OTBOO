package com.codeit.weatherwear.domain.feed.service.impl;

import com.codeit.weatherwear.domain.feed.dto.response.FeedDto;
import com.codeit.weatherwear.domain.feed.entity.Feed;
import com.codeit.weatherwear.domain.feed.entity.FeedLike;
import com.codeit.weatherwear.domain.feed.exception.FeedNotFoundException;
import com.codeit.weatherwear.domain.feed.mapper.FeedLikeMapper;
import com.codeit.weatherwear.domain.feed.mapper.FeedMapper;
import com.codeit.weatherwear.domain.feed.repository.FeedLikeRepository;
import com.codeit.weatherwear.domain.feed.repository.FeedRepository;
import com.codeit.weatherwear.domain.feed.service.FeedLikeService;
import com.codeit.weatherwear.domain.follow.dto.UserSummaryDto;
import com.codeit.weatherwear.domain.ootd.dto.response.OotdDto;
import com.codeit.weatherwear.domain.ootd.service.OotdService;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.exception.UserNotFoundException;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import com.codeit.weatherwear.global.event.dto.FeedLikeEvent;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedLikeServiceImpl implements FeedLikeService {

  private final UserRepository userRepository;
  private final FeedRepository feedRepository;
  private final FeedLikeRepository feedLikeRepository;

  private final OotdService ootdService;

  private final FeedMapper feedMapper;
  private final FeedLikeMapper feedLikeMapper;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Transactional
  @Override
  public FeedDto addFeedLike(UUID feedId, UUID currentUserId) {
    log.debug("Request Add Feed Like - feedId: {}, userId: {}", feedId, currentUserId);
    User user = userRepository.findById(currentUserId).orElseThrow(UserNotFoundException::new);
    Feed feed = feedRepository.findById(feedId)
        .orElseThrow(() -> new FeedNotFoundException(feedId));

    FeedLike feedLike = feedLikeMapper.toEntity(feed, user);
    feedLikeRepository.save(feedLike);
    feed.increaseLikeCount();

    UserSummaryDto userSummaryDto = UserSummaryDto.from(feedLike.getUser());
    List<OotdDto> ootds = ootdService.findOotdByFeedId(feed.getId());
    FeedDto feedDto = feedMapper.toDto(feed, userSummaryDto, null, ootds, true);

    log.info("Add Feed Like Success");
    applicationEventPublisher.publishEvent(
        new FeedLikeEvent(feed.getAuthor().getId(), user.getName(), feedDto.getContent()));
    return feedDto;
  }

  /**
   * 좋아요 취소
   *
   * @param feedId
   * @param currentUserId
   */
  @Transactional
  @Override
  public void deleteFeedLike(UUID feedId, UUID currentUserId) {
    log.debug("Request Delete Feed Like - feedId: {}, userId: {}", feedId, currentUserId);
    Feed feed = feedRepository.findById(feedId)
        .orElseThrow(() -> new FeedNotFoundException(feedId));

    feedLikeRepository.findFeedLikeByFeedIdAndUserId(feedId, currentUserId).ifPresent(
        feedLike -> {
          feed.decreaseLikeCount();
          feedLikeRepository.delete(feedLike);
        }
    );
    log.info("Delete Feed Like Success");
  }

  /**
   * 피드 완전 삭제 시 좋아요도 함께 삭제
   *
   * @param feed
   */
  @Transactional
  @Override
  public void deleteAllFeedLikeInFeed(Feed feed) {
    log.debug("Request Delete All Feed Like - feedId: {}", feed.getId());

    feedLikeRepository.deleteAllByFeed(feed);
    log.info("Delete All Likes in Feed Success");
  }

  @Transactional(readOnly = true)
  @Override
  public boolean isLikedByMe(Feed feed, UUID currentUserId) {
    log.debug("Request isLikedByMe - feedId: {}, userId: {}", feed.getId(), currentUserId);

    User me = userRepository.findById(currentUserId).orElseThrow(UserNotFoundException::new);
    return feedLikeRepository.existsFeedLikeByFeedAndUser(feed, me);
  }
}
