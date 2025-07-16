package com.codeit.weatherwear.domain.notification.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record NotificationRequest(
    String cursor,

    UUID idAfter,

    @NotNull @Min(1) @Max(100)
    int limit
) {

}
