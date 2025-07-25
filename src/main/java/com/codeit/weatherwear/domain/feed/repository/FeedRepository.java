package com.codeit.weatherwear.domain.feed.repository;

import com.codeit.weatherwear.domain.feed.entity.Feed;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedRepository extends JpaRepository<Feed, UUID>, FeedCustomRepository {

  // 현재 Feed에서 사용하고 있는 날씨의 ID 리스트 반환
  @Query("SELECT f.weather.id FROM Feed f WHERE f.weather IN :weatherList")
  List<UUID> findWeatherIdsInUse(@Param("weatherList") List<Weather> weatherList);
}
