package com.codeit.weatherwear.domain.location.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.codeit.weatherwear.domain.location.api.LocationApiClient;
import com.codeit.weatherwear.domain.location.dto.LocationDto;
import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.location.exception.LocationNotFoundException;
import com.codeit.weatherwear.domain.location.repository.LocationRepository;
import com.codeit.weatherwear.domain.location.util.LamcUtils;
import com.codeit.weatherwear.domain.location.util.LamcUtils.GridPoint;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class LocationServiceImplTest {

  @InjectMocks
  private LocationServiceImpl locationService;

  @Mock
  private LocationRepository locationRepository;

  @Mock
  private LamcUtils lamcUtils;

  @Mock
  private LocationApiClient locationApiClient;

  @Test
  @DisplayName("위경도를 통해 위치 엔티티를 찾아 성공적으로 반환한다")
  void getLocation_success() {
    // given
    UUID locationId = UUID.randomUUID();
    double latitude = 27.2, longitude = 39.2;
    Location location = createMockLocation(locationId, latitude, longitude);

    given(locationRepository.findByLatitudeAndLongitude(latitude, longitude)).willReturn(
        Optional.of(location));

    // when
    Location result = locationService.getLocation(latitude, longitude);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(location.getId());
  }

  @Test
  @DisplayName("위경도를 통해 위치 엔티티를 찾았을 때 찾지 못해서 예외를 반환한다")
  void getLocation_failed_cannot_find_lat_long() {
    double latitude = 27.2, longitude = 39.2;

    // given
    given(locationRepository.findByLatitudeAndLongitude(latitude, longitude)).willReturn(
        Optional.empty());

    // when & then
    assertThatThrownBy(() -> locationService.getLocation(latitude, longitude))
        .isInstanceOf(LocationNotFoundException.class);
  }

  @Test
  @DisplayName("LocationDto에 주어진 값과 같은 위치가 있을 시 DB에 존재하는 위치를 가져와 반환한다")
  void findOrCreateLocation_find() {
    // given
    LocationDto mockLocationDto = createMockLocationDto(40.2, 127.9, 10, 20,
        List.of("서울", "송파구", "신천동"));
    Location existingLocation = createMockLocationByDto(mockLocationDto);
    given(locationRepository.findLocationByNameAndLatitudeAndLongitude(
        existingLocation.getName(), existingLocation.getLatitude(),
        existingLocation.getLongitude())).willReturn(
        Optional.of(existingLocation));

    // when
    Location result = locationService.findOrCreateLocation(mockLocationDto);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(existingLocation.getId());
  }

  @Test
  @DisplayName("LocationDto에 주어진 값과 같은 위치가 없어 새로 위치를 생성하여 반환한다")
  void findOrCreateLocation_create() {
    // given
    LocationDto mockLocationDto = createMockLocationDto(40.2, 127.9, 10, 20,
        List.of("서울", "송파구", "신천동"));
    Location createLocation = createMockLocationByDto(mockLocationDto);
    given(locationRepository.findLocationByNameAndLatitudeAndLongitude(
        createLocation.getName(), createLocation.getLatitude(),
        createLocation.getLongitude())).willReturn(
        Optional.empty());
    given(locationRepository.save(any(Location.class))).willReturn(createLocation);

    // when
    Location result = locationService.findOrCreateLocation(mockLocationDto);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(createLocation.getId());
  }

  @Test
  @DisplayName("위치 조회 시 문자열 합침 과정에서 null, 빈 문자열, 공백만 있는 문자열은 제외하고 합친다")
  void findOrCreateLocation_joinLocationNames_filtersNullAndBlank() {
    // given
    // List.of 에 Null을 넣으면 NPE 발생
    List<String> names = Arrays.asList("서울", null, " ", "", "송파구", "   ", "신천동");
    LocationDto mockLocationDto = createMockLocationDto(40.2, 127.9, 10, 20, names);
    Location createLocation = createMockLocationByDto(mockLocationDto);
    given(locationRepository.findLocationByNameAndLatitudeAndLongitude(
        createLocation.getName(), createLocation.getLatitude(),
        createLocation.getLongitude())).willReturn(
        Optional.empty());
    given(locationRepository.save(any(Location.class))).willReturn(createLocation);

    // when
    Location result = locationService.findOrCreateLocation(mockLocationDto);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(createLocation.getId());
    assertThat(result.getName()).isEqualTo("서울 송파구 신천동");
  }


  @Test
  @DisplayName("위경도를 받아 X, Y 값 및 외부 API를 통해 주소 명을 받아와서 위치 조회 혹은 생성하여 반환한다")
  void findOrCreateByGeoPoint() {
    // given
    double latitude = 40.2, longitude = 127.9;
    int x = 10, y = 20;
    GridPoint point = new GridPoint(x, y);
    List<String> addrList = List.of("서울", "송파구", "신천동");
    LocationDto mockLocationDto = createMockLocationDto(latitude, longitude, x, y, addrList);
    Location location = createMockLocationByDto(mockLocationDto);

    given(lamcUtils.convertToGrid(latitude, longitude)).willReturn(point);
    given(locationApiClient.getRegionNames(latitude, longitude)).willReturn(addrList);
    given(locationRepository.findLocationByNameAndLatitudeAndLongitude(
        location.getName(), location.getLatitude(), location.getLongitude())).willReturn(
        Optional.of(location));

    // when
    Location result = locationService.findOrCreateByGeoPoint(latitude, longitude);

    // then
    assertThat(result).isNotNull();
    assertThat(result).isEqualTo(location);
    assertThat(result.getId()).isEqualTo(location.getId());
  }

  // private method ---------------------
  private Location createMockLocation(UUID locationId, double latitude, double longitude) {
    Location location = new Location(latitude, longitude, 100, 20, "서울 종로구 창경궁로");
    ReflectionTestUtils.setField(location, "id", locationId);
    return location;
  }

  private Location createMockLocationByDto(LocationDto locationDto) {
    String addrStr = locationDto.locationNames().stream()
        .filter(s -> s != null && !s.isBlank())
        .collect(Collectors.joining(" "));
    Location location = new Location(locationDto.latitude(), locationDto.longitude(),
        locationDto.x(), locationDto.y(), addrStr);
    ReflectionTestUtils.setField(location, "id", UUID.randomUUID());
    return location;
  }


  private LocationDto createMockLocationDto(double latitude, double longitude, int x, int y,
      List<String> addr) {
    return new LocationDto(latitude, longitude, x, y, addr);
  }

}