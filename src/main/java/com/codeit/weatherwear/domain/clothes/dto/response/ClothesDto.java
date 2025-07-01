package com.codeit.weatherwear.domain.clothes.dto.response;

import com.codeit.weatherwear.domain.clothes.entity.ClothType;
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
    ClothType type;
    List<ClothesAttributeWithDefDto> attributes;
}
