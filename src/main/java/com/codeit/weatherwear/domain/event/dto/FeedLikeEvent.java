package com.codeit.weatherwear.domain.event.dto;

import java.util.UUID;

public record FeedLikeEvent(
    UUID receiverId,
    String likerName,
    String feedContent
) {

}
