package com.codeit.weatherwear.domain.clothes.mapper;

import com.codeit.weatherwear.domain.clothes.dto.request.ClothesAttributeWithDefDto;
import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import com.codeit.weatherwear.domain.clothes.entity.Cloth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClothMapper {
    public ClothesDto toDto(Cloth cloth) {
        List<ClothesAttributeWithDefDto> clothesAttributeWithDefDtos = cloth.getClothesWithAttributes().stream()
            .map(attr -> new ClothesAttributeWithDefDto(
                attr.getAttribute().getId(),
                attr.getAttribute().getName(),
                attr.getAttribute().getSelectableValues(),
                attr.getValue()
            ))
            .toList();

        return ClothesDto.builder()
            .id(cloth.getId())
            .ownerId(cloth.getUser().getId())
            .name(cloth.getName())
            .imageUrl(cloth.getClothesImageUrl())
            .type(cloth.getClothType())
            .attributes(clothesAttributeWithDefDtos)
            .build();

    }
}
