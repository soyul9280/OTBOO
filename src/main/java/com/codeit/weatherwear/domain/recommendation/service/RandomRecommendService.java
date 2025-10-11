package com.codeit.weatherwear.domain.recommendation.service;

import com.codeit.weatherwear.domain.clothes.dto.response.RecommendClothesDto;
import com.codeit.weatherwear.domain.clothes.entity.Cloth;
import com.codeit.weatherwear.domain.clothes.entity.ClothType;
import com.codeit.weatherwear.domain.clothes.entity.ClothWithAttributes;
import com.codeit.weatherwear.domain.clothes.mapper.RecommendClothesMapper;
import com.codeit.weatherwear.domain.clothes.repository.ClothWithAttributesRepository;
import com.codeit.weatherwear.domain.recommendation.dto.response.RecommendationDto;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import com.codeit.weatherwear.global.storage.ThumbnailImageStorage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RandomRecommendService {

  private final RecommendClothesMapper recommendClothesMapper;
  private final ThumbnailImageStorage thumbnailImageStorage;
  private final ClothWithAttributesRepository clothWithAttributesRepository;

  public RecommendationDto recommend(List<Cloth> candidates, User user, Weather weather) {
    // 타입별 그룹핑
    Map<ClothType, List<Cloth>> grouped = candidates.stream()
        .collect(Collectors.groupingBy(Cloth::getClothType));

    List<Cloth> finalRecommendation = new ArrayList<>();

    // DRESS는 먼저 랜덤 선택해본다 (선택되지 않을 수도 있음)
    Optional<Cloth> dress = getRandomCloth(grouped.get(ClothType.DRESS), true);
    // DRESS 처리
    if (dress.isPresent()) {
      finalRecommendation.add(dress.get());
    } else {
      // TOP, BOTTOM = DRESS 없을 때 선택
      Optional<Cloth> top = getRandomCloth(grouped.get(ClothType.TOP), false);
      Optional<Cloth> bottom = getRandomCloth(grouped.get(ClothType.BOTTOM), false);
      top.ifPresent(finalRecommendation::add);
      bottom.ifPresent(finalRecommendation::add);
    }

    // 나머지 ClothType들 처리 (무조건 하나 선택)
    for (ClothType type : ClothType.values()) {
      if (type == ClothType.DRESS || type == ClothType.TOP || type == ClothType.BOTTOM) {
        continue;
      }
      getRandomCloth(grouped.get(type), false).ifPresent(finalRecommendation::add);
    }

    List<UUID> clothIds = finalRecommendation.stream()
        .map(Cloth::getId)
        .toList();

    List<ClothWithAttributes> clothesWithAttrs =
        clothWithAttributesRepository.findByClothIdIn(clothIds);

    // clothId 기준 그룹화
    Map<UUID, List<ClothWithAttributes>> groupedAttrs =
        clothesWithAttrs.stream()
            .collect(Collectors.groupingBy(cwa -> cwa.getCloth().getId()));

    // DTO 변환 + 썸네일 처리
    List<RecommendClothesDto> recommendedClothes = finalRecommendation.stream()
        .map(cloth -> {
          String imageUrl = cloth.getClothesImageUrl() != null
              ? thumbnailImageStorage.get(cloth.getClothesImageUrl())
              : null;
          List<ClothWithAttributes> attrs = groupedAttrs.getOrDefault(cloth.getId(), List.of());

          return recommendClothesMapper.toDto(cloth, imageUrl, attrs);
        })
        .toList();

    log.info("[Recommendation] Recommendation Completed");
    return RecommendationDto.builder()
        .weatherId(weather.getId())
        .userId(user.getId())
        .clothes(List.copyOf(recommendedClothes))//불변성 보장
        .build();
  }


  private Optional<Cloth> getRandomCloth(List<Cloth> clothes,
      boolean allowSkip) {
    if (clothes == null || clothes.isEmpty()) {
      return Optional.empty();
    }

    if (allowSkip) {
      // 예: 50% 확률로 선택 안 할 수 있음
      boolean skip = ThreadLocalRandom.current().nextBoolean();
      if (skip) {
        return Optional.empty();
      }
    }

    return Optional.of(clothes.get(ThreadLocalRandom.current().nextInt(clothes.size())));
  }

}
