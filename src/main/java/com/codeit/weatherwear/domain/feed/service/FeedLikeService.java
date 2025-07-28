package com.codeit.weatherwear.domain.feed.service;

import com.codeit.weatherwear.domain.feed.dto.response.FeedDto;
import com.codeit.weatherwear.domain.feed.entity.Feed;
import java.util.UUID;

public interface FeedLikeService {

  FeedDto addFeedLike(UUID feedId, UUID currentUserId);

  void deleteFeedLike(UUID feedId, UUID currentUserId);

  void deleteAllFeedLikeInFeed(Feed feed);

  boolean isLikedByMe(Feed feed, UUID currentUserId);
}
