package com.codeit.weatherwear.domain.feed.repository;

import com.codeit.weatherwear.domain.feed.dto.condition.FeedSearchCondition;
import com.codeit.weatherwear.domain.feed.entity.Feed;
import org.springframework.data.domain.Slice;

public interface FeedCustomRepository {

  Slice<Feed> searchFeeds(FeedSearchCondition condition);

  long getTotalFeedCount();
}
