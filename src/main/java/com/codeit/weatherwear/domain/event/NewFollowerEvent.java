package com.codeit.weatherwear.domain.event;

import java.util.UUID;

public record NewFollowerEvent(
    UUID receiverId,
    String followerName
) {

}
