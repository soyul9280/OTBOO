package com.codeit.weatherwear.domain.ootd.mapper;

import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import com.codeit.weatherwear.domain.clothes.entity.Cloth;
import com.codeit.weatherwear.domain.clothes.mapper.ClothMapper;
import com.codeit.weatherwear.domain.feed.entity.Feed;
import com.codeit.weatherwear.domain.ootd.dto.response.OotdDto;
import com.codeit.weatherwear.domain.ootd.entity.Ootd;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OotdMapper {

  private final ClothMapper clothMapper;

  public Ootd toEntity(Feed feed, Cloth cloth) {
    return Ootd.builder()
        .feed(feed)
        .cloth(cloth)
        .build();
  }

  public OotdDto toDto(Ootd ootd) {
    ClothesDto clothesDto = clothMapper.toDto(ootd.getCloth());
    return OotdDto.builder()
        .clothesId(clothesDto.getId())
        .name(clothesDto.getName())
        .imageUrl(clothesDto.getImageUrl())
        .type(clothesDto.getType().toString())
        .attributes(clothMapper.toAttributeDefDto(ootd.getCloth().getClothesWithAttributes()))
        .build();
  }

}
