package com.codeit.weatherwear.domain.location.service;

import com.codeit.weatherwear.domain.location.api.LocationApiClient;
import com.codeit.weatherwear.domain.location.dto.LocationDto;
import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.location.mapper.LocationMapper;
import com.codeit.weatherwear.domain.location.repository.LocationRepository;
import com.codeit.weatherwear.domain.location.util.LamcUtils;
import com.codeit.weatherwear.domain.location.util.LamcUtils.GeoPoint;
import com.codeit.weatherwear.domain.location.util.LamcUtils.GridPoint;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationServiceImpl implements LocationService {

  private final LocationRepository locationRepository;
  private final LocationMapper locationMapper;
  private final LamcUtils lamcUtils;
  private final LocationApiClient locationApiClient;

  @Transactional
  @Override
  public Location findOrCreateLocation(LocationDto locationDto) {
    String locationName = locationDto.locationNames().stream()
        .filter(s -> s != null && !s.isBlank())
        .collect(Collectors.joining(" "));
    Location location = new Location(
        locationDto.latitude(),
        locationDto.longitude(),
        locationDto.x(),
        locationDto.y(),
        locationName
    );

    // 이미 존재하면 기존 객체 반환
    return locationRepository.findByLatitudeAndLongitudeAndXAndYAndName(
        location.getLatitude(),
        location.getLongitude(),
        location.getX(),
        location.getY(),
        location.getName()
    ).orElseGet(() -> locationRepository.save(location));
  }

  @Transactional
  @Override
  public Location findOrCreateByXY(int x, int y) {
    GeoPoint geoPoint = lamcUtils.convertToGeo(x, y);
    List<String> regionNames = locationApiClient.getRegionNames(geoPoint.latitude(),
        geoPoint.longitude());
    LocationDto locationDto = new LocationDto(geoPoint.latitude(), geoPoint.longitude(), x, y,
        regionNames);

    // 생성 OR 조회
    Location location = findOrCreateLocation(locationDto);
    return location;
  }

  @Transactional
  @Override
  public Location findOrCreateByGeoPoint(double latitude, double longitude) {
    GridPoint gridPoint = lamcUtils.convertToGrid(latitude, longitude);
    List<String> regionNames = locationApiClient.getRegionNames(latitude, longitude);

    LocationDto locationDto = new LocationDto(latitude, longitude, gridPoint.nx(), gridPoint.ny(),
        regionNames);

    // 생성 OR 조회
    Location location = findOrCreateLocation(locationDto);
    return location;
  }

  @Transactional(readOnly = true)
  @Override
  public Location getLocation(double latitude, double longitude) {
    // todo: exception
    Location location = locationRepository.findByLatitudeAndLongitude(latitude, longitude)
        .orElseThrow(RuntimeException::new);
    return location;
  }
}
