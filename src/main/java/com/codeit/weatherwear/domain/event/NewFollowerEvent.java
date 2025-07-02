package com.codeit.weatherwear.domain.event;

import com.codeit.weatherwear.domain.follow.dto.FollowDto;

public record NewFollowerEvent(
    FollowDto followDto
) {

}
