package com.codeit.weatherwear.domain.recommendation.service;


import com.codeit.weatherwear.domain.clothes.dto.response.RecommendClothesDto;
import com.codeit.weatherwear.domain.clothes.entity.Cloth;
import com.codeit.weatherwear.domain.clothes.entity.ClothType;
import com.codeit.weatherwear.domain.clothes.entity.ClothWithAttributes;
import com.codeit.weatherwear.domain.clothes.mapper.RecommendClothesMapper;
import com.codeit.weatherwear.domain.clothes.repository.ClothRepository;
import com.codeit.weatherwear.domain.recommendation.dto.RecommendationDto;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.exception.UserNotFoundException;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import com.codeit.weatherwear.domain.weather.entity.WindSpeedType;
import com.codeit.weatherwear.domain.weather.exception.WeatherApiResponseException;
import com.codeit.weatherwear.domain.weather.exception.WeatherNotFoundException;
import com.codeit.weatherwear.domain.weather.repository.WeatherRepository;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {

  private final UserRepository userRepository;
  private final WeatherRepository weatherRepository;
  private final ClothRepository clothRepository;
  private final ThumbnailImageStorage thumbnailImageStorage;
  private final RecommendClothesMapper recommendClothesMapper;

  /**
   * 의상 추천
   *
   * @param weatherId 날씨 ID
   * @return 추천 DTO
   */
  @Override
  public RecommendationDto recommendClothes(UUID weatherId) {
    //사용자 조회
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findByEmail(email).orElseThrow(() -> {
      log.warn("[추천 조회 실패] 존재하지 않는 사용자 email: {}", email);
      return new UserNotFoundException();
    });
    Weather weather = weatherRepository.findById(weatherId).orElseThrow(() -> {
      log.warn("[추천 조회 실패] 존재하지 않는 날씨 id: {}", weatherId);
      return new WeatherNotFoundException();
    });

    List<Cloth> clothes = clothRepository.findAllWithAttributesByUserId(user.getId());

    //날씨에 적당한 옷 타입마다 필터링하기
    List<Cloth> filtered = filterCloth(user, weather, clothes);

    // 타입별로 필터링된 옷을 그룹화
    Map<ClothType, List<Cloth>> groupedFilteredClothes = filtered.stream()
        .collect(Collectors.groupingBy(Cloth::getClothType));

    // DRESS는 먼저 랜덤 선택해본다 (선택되지 않을 수도 있음)
    Cloth dressCandidate = getRandomCloth(groupedFilteredClothes.get(ClothType.DRESS), true);
    boolean hasDress = (dressCandidate != null);

    List<Cloth> finalRecommendation = new ArrayList<>();

    // DRESS 처리
    if (hasDress) {
      finalRecommendation.add(dressCandidate);
    } else {
      // TOP, BOTTOM = DRESS 없을 때 선택
      Cloth top = getRandomCloth(groupedFilteredClothes.get(ClothType.TOP), false);
      Cloth bottom = getRandomCloth(groupedFilteredClothes.get(ClothType.BOTTOM), false);
      if (top != null) {
        finalRecommendation.add(top);
      }
      if (bottom != null) {
        finalRecommendation.add(bottom);
      }
    }

    // 나머지 ClothType들 처리 (무조건 하나 선택)
    for (ClothType type : ClothType.values()) {
      if (type == ClothType.DRESS || type == ClothType.TOP || type == ClothType.BOTTOM) {
        continue;
      }
      Cloth selected = getRandomCloth(groupedFilteredClothes.get(type), false);
      if (selected != null) {
        finalRecommendation.add(selected);
      }
    }

    // DTO 변환 + 썸네일 처리
    List<RecommendClothesDto> recommendedClothes = finalRecommendation.stream()
        .map(cloth -> {
          String imageUrl = cloth.getClothesImageUrl() != null
              ? thumbnailImageStorage.get(cloth.getClothesImageUrl())
              : null;
          return recommendClothesMapper.toDto(cloth, imageUrl);
        })
        .toList();

    return RecommendationDto.builder()
        .weatherId(weatherId)
        .userId(user.getId())
        .clothes(recommendedClothes)
        .build();
  }

  private List<Cloth> filterCloth(User user, Weather weather, List<Cloth> cloths) {
    //체감온도 계산 t:온도 v:풍속
    double t = weather.getTemperature().getCurrent();
    double v = weather.getWindSpeed().getSpeed();
    double apparent =
        13.12 + 0.6215 * t - 11.37 * Math.pow(v, 0.16) + 0.3965 * Math.pow(v, 0.16) * t;

    //민감도 보정( 사용자 민감도 가져오기, 없다면 기본값 2 )
    int sensitivity = Optional.ofNullable(user.getTemperatureSensitivity()).orElse(2);
    double adjusted = apparent + (sensitivity - 2) * 2;

    //날씨 조건 추출
    double rainProb = weather.getPrecipitation().getProbability();
    WindSpeedType windSpeedType = weather.getWindSpeed().getSpeedAsWord();

    //옷 필터링
    return cloths.stream()
        .filter(c -> isSuitable(c, adjusted, rainProb, windSpeedType))
        .toList();
  }

  private boolean isSuitable(Cloth cloth, double temp, double rainProb,
      WindSpeedType windSpeedType) {
    // 옷의 속성 리스트를 가져와 Map 형태로 변환하여 쉽게 접근
    List<ClothWithAttributes> clothesWithAttributes = cloth.getClothesWithAttributes();
    Map<String, String> attributeMap = clothesWithAttributes.stream()
        .collect(Collectors.toMap(
            attr -> attr.getAttribute().getName().toLowerCase(), // 속성 정의 이름
            attr -> attr.getValue().toLowerCase()// 속성 값
        ));

    String thickness = attributeMap.get("두께");
    String season = attributeMap.get("계절");
    String waterproof = attributeMap.get("방수");

    if (season != null) {
      switch (season) {
        case "봄":
          if (temp < 5 || temp > 20) {
            return false;
          }
          break;
        case "여름":
          if (temp < 20) {
            return false;
          }
          break;
        case "가을":
          if (temp < 10 || temp > 25) {
            return false;
          }
          break;
        case "겨울":
          if (temp > 10) {
            return false;
          }
          break;
      }
    }

    if (thickness != null) {
      switch (thickness) {
        case "아주 두꺼움":
          if (temp > 5) {
            return false; // 5도 초과면 아주 두꺼운 옷은 부적합
          }
          break;
        case "약간 두꺼움":
          if (temp > 12 || temp < 0) {
            return false; // 0~12도 범위
          }
          break;
        case "약간 얇음":
          if (temp < 15 || temp > 25) {
            return false; // 15~25도 범위
          }
          break;
        case "아주 얇음":
          if (temp < 20) {
            return false; // 20도 미만이면 아주 얇은 옷은 부적합
          }
          break;
      }
    }

    // 3. 강수 확률 필터링: 비 올 확률이 50% 이상인데 방수 속성이 없으면 부적합
    if (rainProb > 50 && waterproof.isBlank()) {
      return false;
    }

    return true;
  }

  private Cloth getRandomCloth(List<Cloth> clothes, boolean allowSkip) {
    if (clothes == null || clothes.isEmpty()) {
      return null;
    }

    if (allowSkip) {
      // 예: 50% 확률로 선택 안 할 수 있음
      boolean skip = ThreadLocalRandom.current().nextBoolean(); // true or false
      if (skip) {
        return null;
      }
    }

    return clothes.get(ThreadLocalRandom.current().nextInt(clothes.size()));
  }
}
