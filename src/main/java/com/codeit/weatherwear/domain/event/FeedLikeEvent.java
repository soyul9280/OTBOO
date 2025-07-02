package com.codeit.weatherwear.domain.event;

import com.codeit.weatherwear.domain.feed.dto.response.FeedDto;
import com.codeit.weatherwear.domain.follow.dto.UserSummaryDto;

public record FeedLikeEvent(
    UserSummaryDto receiver,
    FeedDto feedDto
) {

}
