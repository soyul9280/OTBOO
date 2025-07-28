package com.codeit.weatherwear.domain.clothes.dto.request;

import com.codeit.weatherwear.domain.clothes.entity.ClothType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ClothesSearchRequest(
    @Schema(description = "커서 페이지네이션 커서")
    String cursor,
    @Schema(description ="보조 커서")
    UUID idAfter,
    @Schema(description = "페이지 크기", example = "50")
    @NotNull
    @Min(1)
    @Max(100)
    int limit,
    @Schema(description = "타입", example = "TOP")
    ClothType typeEqual,
    @Schema(description = "사용자ID")
    UUID ownerId
) {
}
