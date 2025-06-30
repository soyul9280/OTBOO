package com.codeit.weatherwear.domain.clothes.dto.response;

import com.codeit.weatherwear.domain.clothes.dto.request.ClothesAttributeWithDefDto;
import com.codeit.weatherwear.domain.clothes.entity.ClothType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClothesDto{
    @NotNull UUID id;
    @NotNull UUID ownerId;
    @NotBlank String name;
    String imageUrl;
    @NotNull ClothType type;
    List<ClothesAttributeWithDefDto> attributes;
}
