package com.codeit.weatherwear.domain.feed.repository;

import com.codeit.weatherwear.domain.feed.dto.condition.FeedSearchCondition;
import com.codeit.weatherwear.domain.feed.entity.Feed;
import java.util.List;

public interface FeedCustomRepository {

  List<Feed> searchFeeds(FeedSearchCondition condition, int limit);
}
