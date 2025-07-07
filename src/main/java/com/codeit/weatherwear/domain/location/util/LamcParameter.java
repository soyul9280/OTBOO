package com.codeit.weatherwear.domain.location.util;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class LamcParameter {

  private double re;     // 사용할 지구반경 [km]
  private double grid;   // 격자간격 [km]
  private double slat1;  // 표준위도1
  private double slat2;  // 표준위도2
  private double olon;   // 기준점 경도
  private double olat;   // 기준점 위도
  private double xo;     // 기준점 X좌표 (격자거리)
  private double yo;     // 기준점 Y좌표 (격자거리)
}
