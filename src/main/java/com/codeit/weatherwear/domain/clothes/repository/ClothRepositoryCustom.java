package com.codeit.weatherwear.domain.clothes.repository;

import com.codeit.weatherwear.domain.clothes.entity.Cloth;
import com.codeit.weatherwear.domain.clothes.entity.ClothType;
import java.time.Instant;
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

}
