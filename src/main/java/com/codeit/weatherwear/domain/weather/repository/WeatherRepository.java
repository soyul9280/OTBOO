package com.codeit.weatherwear.domain.weather.repository;

import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, UUID> {

  List<Weather> findWeatherByLocationAndForecastAt(Location location, Instant forecastAt);

  List<Weather> findWeatherByLocationIdAndForecastAt(UUID locationId, Instant forecastAt,
      Limit limit);

  @Query("SELECT w FROM Weather w WHERE w.location = :location AND w.forecastAt BETWEEN :start AND :end")
  List<Weather> findWeatherByLocationAndForecastRange(
      @Param("location") Location location,
      @Param("start") Instant start,
      @Param("end") Instant end
  );
}
