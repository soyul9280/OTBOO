package com.codeit.weatherwear.domain.event;

import java.util.List;
import java.util.UUID;

public record FolloweeFeedPostedEvent(
    List<UUID> receiverIds,
    String followeeName,
    String content
) {

}
