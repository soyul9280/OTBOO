package com.codeit.weatherwear.domain.event;

import java.util.UUID;

public record ClothAttributeAddedEvent(
    UUID receiverId,
    String attributeName
) {
}
