package com.codeit.weatherwear.domain.clothes.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * 옷 등록 시, 속성정보 넘기기 위함
 *
 * @param definitionId
 * @param value
 */
public record ClothesAttributeDto(
    @NotNull UUID definitionId,
    String value
) {
}
