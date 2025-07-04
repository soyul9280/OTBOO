package com.codeit.weatherwear.domain.event;

import java.util.UUID;

public record FolloweeFeedPostedEvent(
    UUID receiverId,
    String followeeName,
    String content
) {

}
