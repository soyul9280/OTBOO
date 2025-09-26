package com.codeit.weatherwear.domain.clothes.repository;

import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import com.codeit.weatherwear.domain.clothes.entity.Cloth;
import com.codeit.weatherwear.domain.clothes.entity.ClothType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Slice;

public interface ClothRepositoryCustom {

  Slice<Cloth> searchCloths(
      Instant cursor,
      UUID idAfter,
      int limit,
      ClothType typeEqual,
      UUID ownerId
  );

  Long getTotalCount(UUID ownerId, ClothType typeEqual);

 /* Optional<Cloth> findByIdWithAttributes(UUID clothId);

  List<Cloth> findAllByIdWithAttributes(List<UUID> clothIds);

  List<Cloth> findAllWithAttributesByUserId(UUID userId);
*/
}
