package com.codeit.weatherwear.domain.user.repository;

import com.codeit.weatherwear.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, UserCustomRepository {

  boolean existsByName(String name);

  boolean existsByEmail(String email);

  Optional<User> findByEmail(String email);

  @Query("SELECT u.id FROM User u")
  List<UUID> findAllId();

  @Query("SELECT u.id FROM User u WHERE u.id IN :ids")
  List<UUID> findExistingIds(List<UUID> ids);

  Optional<User> findByEmailAndName(String email, String name);
}
