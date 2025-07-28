package com.codeit.weatherwear.domain.user.dto.request;

import com.codeit.weatherwear.domain.user.entity.Role;
import com.codeit.weatherwear.global.request.SortDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "계정 목록 조회 조건")
public record UserSearchRequest(
    String cursor,
    UUID idAfter,
    @NotNull
    @Min(1)
    @Max(100)
    int limit,
    @NotBlank
    String sortBy,
    @NotNull
    @Schema(allowableValues = "ASCENDING, DESCENDING")
    SortDirection sortDirection,
    String emailLike,
    @Schema(allowableValues = "USER, ADMIN")
    Role roleEqual,
    Boolean locked
) {

}