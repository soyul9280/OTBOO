package com.codeit.weatherwear.domain.clothes.dto.request;

import com.codeit.weatherwear.domain.clothes.entity.ClothType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record ClothesCreateRequest(
    @NotNull UUID ownerId,
    @NotBlank(message = "이름 입력은 필수입니다.") String name,
    @NotNull ClothType type,
    List<ClothesAttributeDto> attributes
) {
}
