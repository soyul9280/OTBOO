package com.codeit.weatherwear.domain.follow.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record FollowingRequest(
    @NotNull
    UUID followerId,

    String cursor,

    UUID idAfter,

    @NotNull @Min(1) @Max(100)
    int limit,

    @Size(max = 255)
    String nameLike
) {

}
