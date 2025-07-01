package com.codeit.weatherwear.domain.clothes.dto.response;

import java.util.UUID;

public record ClothesAttributeDto(
    UUID definitionId,
    String value
) {

}
