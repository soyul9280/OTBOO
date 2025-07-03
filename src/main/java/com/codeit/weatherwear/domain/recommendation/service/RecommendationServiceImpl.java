package com.codeit.weatherwear.domain.recommendation.service;


import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import com.codeit.weatherwear.domain.clothes.entity.Cloth;
import com.codeit.weatherwear.domain.clothes.entity.ClothType;
import com.codeit.weatherwear.domain.clothes.mapper.ClothMapper;
import com.codeit.weatherwear.domain.clothes.repository.ClothRepository;
import com.codeit.weatherwear.domain.recommendation.dto.RecommendationDto;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.exception.UserNotFoundException;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import com.codeit.weatherwear.domain.weather.repository.WeatherRepository;
import java.util.List;
import java.util.Map;
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
  private final ClothMapper clothMapper;

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
      //TODO: 날씨 기능 완료 후 변경하기
      return new IllegalArgumentException("존재하지 않는 날씨 입니다.");
    });

    List<Cloth> clothes = clothRepository.findAllWithAttributesByUserId(user.getId());

    Map<ClothType,List<Cloth>> selectCloth=clothes.stream()
        .collect(Collectors.groupingBy(Cloth::getClothType));

    List<ClothesDto> result = selectCloth.values().stream()
        .map(clothsOfType -> {
          Cloth randomCloth = getRandomCloth(clothsOfType);
          return clothMapper.toDto(randomCloth);
        }).toList();

    return RecommendationDto.builder()
        .weatherId(weatherId)
        .userId(user.getId())
        .clothes(result)
        .build();
  }

  private Cloth getRandomCloth(List<Cloth> clothes) {
    return clothes.get(ThreadLocalRandom.current().nextInt(clothes.size()));
  }
}
