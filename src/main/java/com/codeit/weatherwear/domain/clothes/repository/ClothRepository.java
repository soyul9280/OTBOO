package com.codeit.weatherwear.domain.clothes.repository;

import com.codeit.weatherwear.domain.clothes.entity.Cloth;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClothRepository extends JpaRepository<Cloth, UUID>, ClothRepositoryCustom {

  @EntityGraph(attributePaths = {"clothesWithAttributes", "clothesWithAttributes.attribute"})
  Optional<Cloth> findByIdWithAttributes(UUID id);
}
