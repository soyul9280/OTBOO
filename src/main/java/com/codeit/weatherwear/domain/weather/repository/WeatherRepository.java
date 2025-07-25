package com.codeit.weatherwear.domain.weather.repository;

import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, UUID> {

  @Query("SELECT w FROM Weather w WHERE w.location = :location AND w.forecastedAt >= :start AND w.forecastAt >= :start")
  List<Weather> findRecentWeathers(@Param("location") Location location,
      @Param("start") Instant todayStart);

  @Query("SELECT w FROM Weather w WHERE w.location = :location AND w.forecastAt < :target")
  List<Weather> getOldForecast(@Param("location") Location location,
      @Param("target") Instant cutoffTime);

  /**
   * 1. 피드와 연결되어 있는 날씨가 아니고
   * <p>
   * 2. 예보 요청 시간(forecastedAt)이 기준 시간(조회 당일) 이전이거나 예보 시간(forecastAt)이 기준 시간(조회 당일) 이전이라면 삭제
   */
  @Modifying
  @Query("""
      DELETE FROM Weather w
          WHERE w.location = :location
             AND (w.forecastAt < :cutOffTime OR w.forecastedAt < :cutOffTime)
             AND NOT EXISTS (SELECT 1 FROM Feed f WHERE f.weather.id = w.id)""")
  int deleteOldOrphanForecast(@Param("location") Location location,
      @Param("cutOffTime") Instant cutOffTime);
}
