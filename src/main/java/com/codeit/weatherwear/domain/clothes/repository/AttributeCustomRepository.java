package com.codeit.weatherwear.domain.clothes.repository;

import com.codeit.weatherwear.domain.clothes.entity.Attribute;

import com.codeit.weatherwear.global.request.SortDirection;
import java.util.UUID;
import org.springframework.data.domain.Slice;

public interface AttributeCustomRepository {
    Slice<Attribute> searchAttributes(
        String cursor,
        UUID idAfter,
        int limit,
        String sortBy,
        SortDirection sortDirection,
        String keywordLike
    );
    Long getTotalCount(String keywordLike);
}