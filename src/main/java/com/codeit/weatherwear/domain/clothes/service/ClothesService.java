package com.codeit.weatherwear.domain.clothes.service;

import com.codeit.weatherwear.domain.clothes.dto.request.ClothesCreateRequest;
import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import java.util.UUID;

public interface ClothesService {
    ClothesDto create(ClothesCreateRequest request);
    void delete(UUID clothesId);
}
