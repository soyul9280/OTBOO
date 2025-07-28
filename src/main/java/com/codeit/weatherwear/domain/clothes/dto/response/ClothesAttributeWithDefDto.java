package com.codeit.weatherwear.domain.clothes.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * 옷 등록 시, 속성 정보 넘기기 위함
 *
 * @param definitionId
 * @param definitionName
 * @param selectableValues
 * @param value
 */
public record ClothesAttributeWithDefDto(
    UUID definitionId,
    String definitionName,
    List<String> selectableValues,
    String value
) {

}
