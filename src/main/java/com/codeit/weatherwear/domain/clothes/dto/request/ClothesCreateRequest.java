package com.codeit.weatherwear.domain.clothes.dto.request;

import com.codeit.weatherwear.domain.clothes.entity.ClothType;
import java.util.List;
import java.util.UUID;

public record ClothesCreateRequest(
    UUID ownerId,
    String name,
    ClothType type,
    List<ClothesAttributeDto> attributes
) {
}
