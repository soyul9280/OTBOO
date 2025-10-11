package com.codeit.weatherwear.domain.clothes.mapper;

import com.codeit.weatherwear.domain.clothes.dto.response.ClothesAttributeWithDefDto;
import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import com.codeit.weatherwear.domain.clothes.entity.Cloth;
import com.codeit.weatherwear.domain.clothes.entity.ClothWithAttributes;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClothMapper {

  public ClothesDto toDto(Cloth cloth, String imageUrl, List<ClothWithAttributes> attributes) {
    return ClothesDto.builder()
        .id(cloth.getId())
        .ownerId(cloth.getUser().getId())
        .name(cloth.getName())
        .imageUrl(imageUrl)
        .type(cloth.getClothType())
        .attributes(toAttributeDefDto(attributes))
        .build();

  }

  public List<ClothesAttributeWithDefDto> toAttributeDefDto(List<ClothWithAttributes> attrs) {
    return attrs
        .stream().map(
            attr -> new ClothesAttributeWithDefDto(
                attr.getAttribute().getId(),
                attr.getAttribute().getName(),
                attr.getAttribute().getSelectableValues(),
                attr.getValue()
            )
        )
        .toList();
  }
}
