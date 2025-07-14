package com.codeit.weatherwear.domain.ootd.mapper;

import com.codeit.weatherwear.domain.clothes.entity.Cloth;
import com.codeit.weatherwear.domain.clothes.mapper.ClothMapper;
import com.codeit.weatherwear.domain.feed.entity.Feed;
import com.codeit.weatherwear.domain.ootd.dto.response.OotdDto;
import com.codeit.weatherwear.domain.ootd.entity.Ootd;
import com.codeit.weatherwear.global.storage.ThumbnailImageStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OotdMapper {

  private final ClothMapper clothMapper;
  private final ThumbnailImageStorage thumbnailImageStorage;

  /**
   * Creates an Ootd entity by associating the specified Feed and Cloth.
   *
   * @param feed  the Feed to associate with the Ootd
   * @param cloth the Cloth to associate with the Ootd
   * @return a new Ootd entity linked to the given Feed and Cloth
   */
  public Ootd toEntity(Feed feed, Cloth cloth) {
    return Ootd.builder()
        .feed(feed)
        .cloth(cloth)
        .build();
  }

  /**
   * Converts an {@link Ootd} entity to an {@link OotdDto}, including cloth details and a resolved thumbnail image URL if available.
   *
   * @param ootd the OOTD entity to convert
   * @return an OotdDto containing the cloth's ID, name, thumbnail image URL (if present), type, and attribute definitions
   */
  public OotdDto toDto(Ootd ootd) {
    Cloth cloth = ootd.getCloth();
    String imageUrl =
        cloth.getClothesImageUrl() != null ? thumbnailImageStorage.get(cloth.getClothesImageUrl())
            : null;

    return OotdDto.builder()
        .clothesId(cloth.getId())
        .name(cloth.getName())
        .imageUrl(imageUrl)
        .type(cloth.getClothType().toString())
        .attributes(clothMapper.toAttributeDefDto(cloth.getClothesWithAttributes()))
        .build();
  }

}
