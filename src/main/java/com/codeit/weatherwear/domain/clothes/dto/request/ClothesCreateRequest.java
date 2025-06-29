package com.codeit.weatherwear.domain.clothes.dto.request;

import com.codeit.weatherwear.domain.clothes.entity.ClothesType;
import java.util.List;
import java.util.UUID;

public record ClothesCreateRequest(
    UUID ownerId,
    String name,
    ClothesType type,
    List<ClothesAttributeDto> attributes
) {
}
