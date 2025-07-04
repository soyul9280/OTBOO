package com.codeit.weatherwear.domain.event;

import java.util.UUID;

public record NewFeedCommentEvent(
    UUID receiverId,
    String authorName,
    String commentContent
) {

}
