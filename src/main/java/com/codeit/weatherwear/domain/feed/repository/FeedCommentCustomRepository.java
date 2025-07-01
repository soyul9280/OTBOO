package com.codeit.weatherwear.domain.feed.repository;

import com.codeit.weatherwear.domain.feed.dto.condition.FeedCommentSearchCondition;
import com.codeit.weatherwear.domain.feed.entity.FeedComment;
import java.util.UUID;
import org.springframework.data.domain.Slice;

public interface FeedCommentCustomRepository {

  Slice<FeedComment> searchFeedComments(FeedCommentSearchCondition condition);

  long getTotalFeedCommentCount(UUID feedId);
}
