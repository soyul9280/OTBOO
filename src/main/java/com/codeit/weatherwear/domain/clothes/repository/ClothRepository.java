package com.codeit.weatherwear.domain.clothes.repository;

import com.codeit.weatherwear.domain.clothes.entity.Cloth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ClothRepository extends JpaRepository<Cloth, UUID>, ClothRepositoryCustom {

  List<Cloth> findAllByUserId(UUID userId);
}
