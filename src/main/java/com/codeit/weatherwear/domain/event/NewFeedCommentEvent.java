package com.codeit.weatherwear.domain.event;

import com.codeit.weatherwear.domain.feed.dto.response.FeedCommentDto;
import com.codeit.weatherwear.domain.follow.dto.UserSummaryDto;

public record NewFeedCommentEvent(
    UserSummaryDto receiver,
    FeedCommentDto feedCommentDto
) {

}
