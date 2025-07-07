package com.codeit.weatherwear.domain.event.dto;

import java.util.UUID;

public record NewFollowerEvent(
    UUID receiverId,
    String followerName
) {

}
