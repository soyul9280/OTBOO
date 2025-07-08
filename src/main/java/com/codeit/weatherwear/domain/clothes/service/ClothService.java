package com.codeit.weatherwear.domain.clothes.service;

import com.codeit.weatherwear.domain.clothes.dto.request.ClothesCreateRequest;
import com.codeit.weatherwear.domain.clothes.dto.request.ClothesSearchRequest;
import com.codeit.weatherwear.domain.clothes.dto.request.ClothesUpdateRequest;
import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import com.codeit.weatherwear.global.response.PageResponse;
import java.util.UUID;

public interface ClothService {
    ClothesDto create(ClothesCreateRequest request);
    ClothesDto createFromUrl(String url);
    ClothesDto update(UUID clothesId,ClothesUpdateRequest request);
    PageResponse<ClothesDto> searchClothes(ClothesSearchRequest request);
    void delete(UUID clothesId);
}
