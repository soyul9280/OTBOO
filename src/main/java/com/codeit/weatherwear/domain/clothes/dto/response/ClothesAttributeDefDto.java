package com.codeit.weatherwear.domain.clothes.dto.response;


import java.util.List;
import java.util.UUID;

/**
 * 속성정의 DTO
 *
 * @param id
 * @param name
 * @param selectableValues
 */
public record ClothesAttributeDefDto(
    UUID id,
    String name,
    List<String> selectableValues
) {

}
