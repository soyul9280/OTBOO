package com.codeit.weatherwear.domain.user.repository;

import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, UserCustomRepository {

  Optional<User> findById(UUID id);

  @Query("SELECT u FROM User u LEFT JOIN FETCH u.location WHERE u.id = :id")
  Optional<User> findByIdWithLocation(UUID id);

  boolean existsByName(String name);

  boolean existsByEmail(String email);

  Optional<User> findByEmail(String email);

  @Query("SELECT u.id FROM User u")
  List<UUID> findAllId();

  @Query("SELECT u.id FROM User u WHERE u.id IN :ids")
  List<UUID> findExistingIds(List<UUID> ids);

  @Query("SELECT DISTINCT u.id FROM User u WHERE u.location = :location")
  List<UUID> findUserIdsByLocation(@Param("location") Location location);

}
