package com.codeit.weatherwear.domain.location.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.within;

import com.codeit.weatherwear.domain.location.util.LamcUtils.GeoPoint;
import com.codeit.weatherwear.domain.location.util.LamcUtils.GridPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class LamcUtilsTest {

  @InjectMocks
  private LamcUtils lamcUtils;

  @Test
  @DisplayName("위경도 → 격자 → 위경도 변환 시 원래 좌표와 유사해야 함")
  void convertToGridAndBack_preservesCoordinatesRoughly() {
    // given
    double latitude = 37.5665; // 서울시청 위도
    double longitude = 126.9780; // 서울시청 경도

    // when
    GridPoint grid = lamcUtils.convertToGrid(latitude, longitude);
    GeoPoint geo = lamcUtils.convertToGeo(grid.nx(), grid.ny());

    // then
    assertThat(geo.latitude()).isCloseTo(latitude, within(0.1)); // ±0.1도
    assertThat(geo.longitude()).isCloseTo(longitude, within(0.1));
  }

}