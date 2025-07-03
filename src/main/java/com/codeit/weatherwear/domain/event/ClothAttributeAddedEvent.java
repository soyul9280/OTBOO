package com.codeit.weatherwear.domain.event;

import com.codeit.weatherwear.domain.follow.dto.UserSummaryDto;

public record ClothAttributeAddedEvent(
    UserSummaryDto receiver,
    String attributeName
) {
}
