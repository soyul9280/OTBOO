package com.codeit.weatherwear.domain.directmessage;

import com.codeit.weatherwear.domain.follow.dto.UserSummaryDto;
import java.time.Instant;
import java.util.UUID;

public record DirectMessageDto (
    UUID id,
    Instant createdAt,
    UserSummaryDto sender,
    UserSummaryDto receiver,
    String content
) {

}
