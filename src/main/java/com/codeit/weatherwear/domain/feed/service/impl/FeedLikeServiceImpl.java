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
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  @Transactional
  @Override
  public FeedDto addFeedLike(UUID feedId, UUID currentUserId) {
    User user = userRepository.findById(currentUserId).orElseThrow(UserNotFoundException::new);
    Feed feed = feedRepository.findById(feedId)
        .orElseThrow(() -> new FeedNotFoundException(feedId));

    FeedLike feedLike = feedLikeMapper.toEntity(feed, user);
    feedLikeRepository.save(feedLike);
    feed.increaseLikeCount();

    UserSummaryDto userSummaryDto = UserSummaryDto.from(feedLike.getUser());
    List<OotdDto> ootds = ootdService.findOotdByFeedId(feed.getId());

    return feedMapper.toDto(feed, userSummaryDto, null, ootds, true);
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
    Feed feed = feedRepository.findById(feedId)
        .orElseThrow(() -> new FeedNotFoundException(feedId));

    feedLikeRepository.findFeedLikeByFeedIdAndUserId(feedId, currentUserId).ifPresent(
        feedLike -> {
          feed.decreaseLikeCount();
          feedLikeRepository.delete(feedLike);
        }
    );
  }

  /**
   * 피드 완전 삭제 시 좋아요도 함께 삭제
   *
   * @param feed
   */
  @Transactional
  @Override
  public void deleteAllFeedLikeInFeed(Feed feed) {
    feedLikeRepository.deleteAllByFeed(feed);
  }

  @Transactional(readOnly = true)
  @Override
  public boolean isLikedByMe(Feed feed, UUID currentUserId) {
    User me = userRepository.findById(currentUserId).orElseThrow(UserNotFoundException::new);
    return feedLikeRepository.existsFeedLikeByFeedAndUser(feed, me);
  }
}
