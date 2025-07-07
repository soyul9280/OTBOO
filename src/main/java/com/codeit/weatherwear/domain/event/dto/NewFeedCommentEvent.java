package com.codeit.weatherwear.domain.event.dto;

import java.util.UUID;

public record NewFeedCommentEvent(
    UUID receiverId,
    String authorName,
    String commentContent
) {

}
