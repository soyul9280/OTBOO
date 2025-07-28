package com.codeit.weatherwear.domain.weather.repository;

import com.codeit.weatherwear.domain.weather.entity.WeatherApiData;
import com.codeit.weatherwear.domain.weather.entity.WeatherApiDataId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherApiDataRepository extends JpaRepository<WeatherApiData, WeatherApiDataId> {

}
