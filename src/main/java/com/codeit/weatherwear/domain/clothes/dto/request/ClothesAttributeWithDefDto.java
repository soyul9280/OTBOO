package com.codeit.weatherwear.domain.clothes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record ClothesAttributeWithDefDto(
    @NotNull UUID definitionId,
    @NotBlank String definitionName,
    @NotNull List<String> selectableValues,
    String value
) {

}
