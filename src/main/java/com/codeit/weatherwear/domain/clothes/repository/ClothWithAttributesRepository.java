package com.codeit.weatherwear.domain.clothes.repository;

import com.codeit.weatherwear.domain.clothes.entity.ClothWithAttributes;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClothWithAttributesRepository extends JpaRepository<ClothWithAttributes, UUID> {

  @Query("SELECT DISTINCT cwa.value FROM ClothWithAttributes cwa " +
      "WHERE cwa.attribute.id = :attributeId")
  List<String> findUsedValuesByAttribute(@Param("attributeId") UUID attributeId);

  @Query("SELECT DISTINCT cwa FROM ClothWithAttributes cwa " +
      "JOIN FETCH cwa.attribute " +
      "WHERE cwa.cloth.id IN :clothIds")
  List<ClothWithAttributes> findByClothIdIn(@Param("clothIds") List<UUID> clothIds);

  void deleteAllByClothId(UUID clothId);
}
