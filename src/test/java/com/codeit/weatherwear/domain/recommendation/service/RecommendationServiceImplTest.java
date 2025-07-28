package com.codeit.weatherwear.domain.recommendation.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.weatherwear.domain.clothes.dto.response.RecommendClothesDto;
import com.codeit.weatherwear.domain.clothes.entity.Attribute;
import com.codeit.weatherwear.domain.clothes.entity.Cloth;
import com.codeit.weatherwear.domain.clothes.entity.ClothType;
import com.codeit.weatherwear.domain.clothes.entity.ClothWithAttributes;
import com.codeit.weatherwear.domain.clothes.mapper.RecommendClothesMapper;
import com.codeit.weatherwear.domain.clothes.repository.ClothRepository;
import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.recommendation.dto.response.RecommendationDto;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.exception.UserNotFoundException;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import com.codeit.weatherwear.domain.weather.entity.Humidity;
import com.codeit.weatherwear.domain.weather.entity.Precipitation;
import com.codeit.weatherwear.domain.weather.entity.Temperature;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import com.codeit.weatherwear.domain.weather.entity.WindSpeed;
import com.codeit.weatherwear.domain.weather.exception.WeatherNotFoundException;
import com.codeit.weatherwear.domain.weather.repository.WeatherRepository;
import com.codeit.weatherwear.global.storage.ThumbnailImageStorage;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private WeatherRepository weatherRepository;

  @Mock
  private ClothRepository clothRepository;

  @Mock
  private ThumbnailImageStorage thumbnailImageStorage;

  @Mock
  private RecommendClothesMapper recommendClothesMapper;

  @Mock
  private RandomRecommendService randomRecommendService;

  @Mock
  private AIRecommendationService aiRecommendationService;

  @InjectMocks
  private RecommendationServiceImpl sut;

  private UUID weatherId = UUID.randomUUID();
  private UUID userId = UUID.randomUUID();
  private User mockUser;
  private Weather mockWeather;
  private Cloth dress;
  private Cloth top;
  private Cloth bottom;
  private Cloth hat;
  private String email = "test@email.com";
  private String seasonAttr = "계절";
  private String thicknessAttr = "두께";
  List<Cloth> all;

  @BeforeEach
  void setUp() {
    SecurityContext context = mock(SecurityContext.class);
    Authentication auth = mock(Authentication.class);
    when(auth.getName()).thenReturn(email);
    when(context.getAuthentication()).thenReturn(auth);
    SecurityContextHolder.setContext(context);

    mockUser = User.builder().id(userId).temperatureSensitivity(2).build();
    //여름
    mockWeather = Weather.builder()
        .temperature(Temperature.builder().current(27.0).build())
        .precipitation(Precipitation.builder().probability(0.1).build())
        .windSpeed(WindSpeed.builder().speed(1.0).build())
        .forecastAt(Instant.now())
        .forecastedAt(Instant.now())
        .location(mock(Location.class))
        .build();

    dress = makeCloth(ClothType.DRESS, "여름", "아주 얇음");
    top = makeCloth(ClothType.TOP, "여름", "아주 얇음");
    //Bottom은 추천하면 안됨
    bottom = makeCloth(ClothType.BOTTOM, "겨울", "아주 두꺼움");
    hat = makeCloth(ClothType.HAT, "여름", "아주 얇음");
    all = List.of(dress, top, bottom, hat);
  }

  @Test
  @DisplayName("추천 성공 - DRESS 타입 선택될수도 안될수도 있음 & hat은 항상 추천")
  void recommend_withDress() {
    /** given **/
    Instant summerDate = LocalDate.of(2025, 7, 17).atStartOfDay(ZoneId.systemDefault()).toInstant();
    mockWeather = Weather.builder()
        .temperature(Temperature.builder().current(27.0).build())
        .humidity(Humidity.builder().current(60.0).build()) // 습구온도에 영향
        .precipitation(Precipitation.builder().probability(0.0).build())
        .windSpeed(WindSpeed.builder().speed(1.0).build())
        .forecastAt(summerDate)
        .forecastedAt(summerDate)
        .location(mock(Location.class))
        .build();

    // 사용자 민감도: 보통()
    mockUser = User.builder().id(userId).temperatureSensitivity(2).build();

    // 옷 구성: 여름 얇은 옷 3개 + 겨울 두꺼운 옷 1개(bottom)
    Cloth summerDress = makeCloth(ClothType.DRESS, "여름", "아주 얇음");
    Cloth summerTop = makeCloth(ClothType.TOP, "여름", "얇음");
    Cloth summerHat = makeCloth(ClothType.HAT, "여름", "아주 얇음");
    Cloth winterBottom = makeCloth(ClothType.BOTTOM, "겨울", "아주 두꺼움"); // 필터링 대상

    List<Cloth> all = List.of(summerDress, summerTop, summerHat, winterBottom);

    // 예상 결과 DTO
    RecommendClothesDto dressDto = RecommendClothesDto.builder()
        .name("dress")
        .imageUrl("dress_url")
        .build();
    RecommendClothesDto hatDto = RecommendClothesDto.builder()
        .name("hat")
        .imageUrl("hat_url")
        .build();

    Map<Cloth, RecommendClothesDto> dtoMap = Map.of(
        summerDress, dressDto,
        summerHat, hatDto
    );

    given(userRepository.findByEmail(email)).willReturn(Optional.of(mockUser));
    given(weatherRepository.findById(weatherId)).willReturn(Optional.of(mockWeather));
    given(clothRepository.findAllWithAttributesByUserId(userId)).willReturn(all);
    given(aiRecommendationService.getRecommendationCandidates(anyList(), eq(mockUser),
        eq(mockWeather))).willReturn(List.of(summerDress, summerHat, summerTop));
    given(randomRecommendService.recommend(anyList(), eq(mockUser), eq(mockWeather)))
        .willAnswer(invocation -> {
          List<Cloth> filtered = invocation.getArgument(0);
          return RecommendationDto.builder()
              .userId(mockUser.getId())
              .weatherId(mockWeather.getId())
              .clothes(filtered.stream().map(dtoMap::get).toList())
              .build();
        });

    /** when **/
    RecommendationDto result = sut.recommendClothes(weatherId);

    /**
     * then
     **/
    assertThat(result).isNotNull();
    List<RecommendClothesDto> clothes = result.getClothes();
    assertThat(clothes).contains(dressDto, hatDto);

    // winterBottom은 아주 두꺼움 + 겨울이라 필터링돼야 함
    assertThat(clothes).doesNotContain(RecommendClothesDto.builder()
        .name("bottom")
        .imageUrl("bottom_url")
        .build());
  }

  @Test
  @DisplayName("추천 실패 - 사용자 없음")
  void recommend_user_not_found() {
    //given
    given(userRepository.findByEmail(any())).willReturn(Optional.empty());
    //when
    //then
    assertThatThrownBy(() -> sut.recommendClothes(weatherId))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessage("사용자 확인 실패");
    verify(recommendClothesMapper, never()).toDto(any(), any());
    verify(userRepository, times(1)).findByEmail(any());
    verify(weatherRepository, never()).findById(any());
    verify(clothRepository, never()).findById(any());
    verify(thumbnailImageStorage, never()).get(any());
  }

  @Test
  @DisplayName("추천 실패 - 날씨 없음")
  void recommend_weather_not_found() {
    //given
    given(userRepository.findByEmail(any())).willReturn(Optional.of(mockUser));
    given(weatherRepository.findById(any())).willReturn(Optional.empty());
    //when
    //then
    assertThatThrownBy(() -> sut.recommendClothes(weatherId))
        .isInstanceOf(WeatherNotFoundException.class)
        .hasMessage("날씨 확인 실패");
    verify(recommendClothesMapper, never()).toDto(any(), any());
    verify(userRepository, times(1)).findByEmail(any());
    verify(weatherRepository, times(1)).findById(any());
    verify(clothRepository, never()).findById(any());
    verify(thumbnailImageStorage, never()).get(any());
  }


  private Cloth makeCloth(ClothType type, String seasonValue, String thicknessValue) {
    Cloth cloth = Cloth.builder()
        .id(UUID.randomUUID())
        .name(type.name())
        .clothType(type)
        .user(mockUser)
        .clothesWithAttributes(List.of(
            makeAttribute(seasonAttr, List.of("여름", "겨울"), seasonValue),
            makeAttribute(thicknessAttr, List.of("아주 얇음", "두꺼움"), thicknessValue)
        ))
        .build();
    return cloth;
  }

  private ClothWithAttributes makeAttribute(String name, List<String> selectableValues,
      String value) {
    return ClothWithAttributes.builder()
        .attribute(Attribute.builder().name(name).selectableValues(selectableValues).build())
        .value(value)
        .build();
  }

  private RecommendClothesDto buildDto(Cloth cloth) {
    return RecommendClothesDto.builder()
        .clothesId(cloth.getId())
        .name(cloth.getName())
        .type(cloth.getClothType())
        .imageUrl(null)
        .build();
  }


}