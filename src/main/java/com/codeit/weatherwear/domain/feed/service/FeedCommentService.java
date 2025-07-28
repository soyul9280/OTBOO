package com.codeit.weatherwear.domain.feed.service;

import com.codeit.weatherwear.domain.feed.dto.request.FeedCommentCreateRequest;
import com.codeit.weatherwear.domain.feed.dto.request.FeedCommentGetParamRequest;
import com.codeit.weatherwear.domain.feed.dto.response.FeedCommentDto;
import com.codeit.weatherwear.domain.feed.entity.Feed;
import com.codeit.weatherwear.global.response.PageResponse;
import java.util.UUID;

public interface FeedCommentService {

  FeedCommentDto createFeedComment(UUID feedId, FeedCommentCreateRequest request);

  PageResponse<FeedCommentDto> getFeedComments(UUID feedId,
      FeedCommentGetParamRequest queryRequest);

  void deleteFeedCommentsByFeed(Feed feed);

}
