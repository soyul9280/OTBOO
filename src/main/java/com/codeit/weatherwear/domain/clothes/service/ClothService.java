package com.codeit.weatherwear.domain.clothes.service;

import com.codeit.weatherwear.domain.clothes.dto.request.ClothesCreateRequest;
import com.codeit.weatherwear.domain.clothes.dto.request.ClothesSearchRequest;
import com.codeit.weatherwear.domain.clothes.dto.request.ClothesUpdateRequest;
import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import com.codeit.weatherwear.global.response.PageResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface ClothService {

  ClothesDto create(ClothesCreateRequest request, MultipartFile image);

  ClothesDto getFromUrl(String url);

  ClothesDto update(UUID clothesId, ClothesUpdateRequest request, MultipartFile image);

  PageResponse<ClothesDto> searchClothes(ClothesSearchRequest request);

  void delete(UUID clothesId);
}
