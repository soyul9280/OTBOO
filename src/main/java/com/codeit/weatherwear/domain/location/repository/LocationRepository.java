package com.codeit.weatherwear.domain.location.repository;

import com.codeit.weatherwear.domain.location.entity.Location;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {

  Optional<Location> findLocationByName(String name);

  Optional<Location> findByLatitudeAndLongitude(double latitude, double longitude);

  boolean existsLocationByLatitudeAndLongitude(double latitude, double longitude);
}
