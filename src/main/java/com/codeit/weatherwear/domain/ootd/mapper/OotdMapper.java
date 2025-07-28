package com.codeit.weatherwear.domain.ootd.mapper;

import com.codeit.weatherwear.domain.clothes.dto.response.ClothesAttributeWithDefDto;
import com.codeit.weatherwear.domain.clothes.entity.Cloth;
import com.codeit.weatherwear.domain.feed.entity.Feed;
import com.codeit.weatherwear.domain.ootd.dto.response.OotdDto;
import com.codeit.weatherwear.domain.ootd.entity.Ootd;
import com.codeit.weatherwear.global.storage.ThumbnailImageStorage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OotdMapper {

  private final ThumbnailImageStorage thumbnailImageStorage;

  public Ootd toEntity(Feed feed, Cloth cloth) {
    return Ootd.builder()
        .feed(feed)
        .cloth(cloth)
        .build();
  }

  public OotdDto toDto(Ootd ootd) {
    Cloth cloth = ootd.getCloth();
    String imageUrl =
        cloth.getClothesImageUrl() != null ? thumbnailImageStorage.get(cloth.getClothesImageUrl())
            : null;

    List<ClothesAttributeWithDefDto> clothesAttributeWithDefDtos = cloth.getClothesWithAttributes()
        .stream()
        .map(attr -> new ClothesAttributeWithDefDto(
            attr.getAttribute().getId(),
            attr.getAttribute().getName(),
            attr.getAttribute().getSelectableValues(),
            attr.getValue()
        ))
        .toList();

    return OotdDto.builder()
        .clothesId(cloth.getId())
        .name(cloth.getName())
        .imageUrl(imageUrl)
        .type(cloth.getClothType().toString())
        .attributes(clothesAttributeWithDefDtos)
        .build();
  }

}
