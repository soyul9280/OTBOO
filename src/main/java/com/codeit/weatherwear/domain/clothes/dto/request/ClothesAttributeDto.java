package com.codeit.weatherwear.domain.clothes.dto.request;

import java.util.UUID;

public record ClothesAttributeDto(
    UUID definitionId,
    String value
) {

}
