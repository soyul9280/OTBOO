package com.codeit.weatherwear.global.event.dto;

import java.util.List;
import java.util.UUID;

public record FolloweeFeedPostedEvent(
    List<UUID> receiverIds,
    String followeeName,
    String content
) implements DomainEvent {

}
