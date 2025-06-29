package com.codeit.weatherwear.domain.clothes.dto.response;

import com.codeit.weatherwear.domain.clothes.dto.request.ClothesAttributeWithDefDto;
import com.codeit.weatherwear.domain.clothes.entity.ClothesType;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClothesDto{
    UUID id;
    UUID ownerId;
    String name;
    String imageUrl;
    ClothesType type;
    List<ClothesAttributeWithDefDto> attributes;
}
