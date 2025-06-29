package com.codeit.weatherwear.domain.clothes.mapper;

import com.codeit.weatherwear.domain.clothes.dto.request.ClothesAttributeWithDefDto;
import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import com.codeit.weatherwear.domain.clothes.entity.Clothes;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClothesMapper {
    public ClothesDto toDto(Clothes clothes) {
        List<ClothesAttributeWithDefDto> clothesAttributeWithDefDtos = clothes.getClothesWithAttributes().stream()
            .map(attr -> new ClothesAttributeWithDefDto(
                attr.getAttributes().getId(),
                attr.getAttributes().getName(),
                attr.getAttributes().getSelectableValues(),
                attr.getValue()
            ))
            .toList();

        return ClothesDto.builder()
            .id(clothes.getId())
            .ownerId(clothes.getUser().getId())
            .name(clothes.getName())
            .imageUrl(clothes.getClothesImageUrl())
            .type(clothes.getClothesType())
            .attributes(clothesAttributeWithDefDtos)
            .build();

    }
}
