package com.codeit.weatherwear.domain.clothes.mapper;

import com.codeit.weatherwear.domain.clothes.dto.response.ClothesAttributeDefDto;
import com.codeit.weatherwear.domain.clothes.entity.Attribute;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AttributeMapper {
    ClothesAttributeDefDto toDto(Attribute attribute);
}
