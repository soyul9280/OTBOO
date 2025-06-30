package com.codeit.weatherwear.domain.clothes.repository;

import com.codeit.weatherwear.domain.clothes.entity.Attribute;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttributeRepository extends JpaRepository<Attribute, UUID>,AttributeCustomRepository {
    Optional<Attribute> findByName(String name);
    boolean existsByName(String name);
}
