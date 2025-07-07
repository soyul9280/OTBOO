package com.codeit.weatherwear.domain.event;

import java.util.UUID;

public record FeedLikeEvent(
    UUID receiverId,
    String likerName,
    String feedContent
) {

}
