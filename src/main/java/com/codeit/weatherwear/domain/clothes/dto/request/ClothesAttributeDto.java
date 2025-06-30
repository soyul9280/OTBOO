package com.codeit.weatherwear.domain.clothes.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ClothesAttributeDto(
    @NotNull UUID definitionId,
    String value
) {

}
