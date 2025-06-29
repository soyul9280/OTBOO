package com.codeit.weatherwear.domain.clothes.controller;

import com.codeit.weatherwear.domain.clothes.controller.api.ClothesApi;
import com.codeit.weatherwear.domain.clothes.dto.request.ClothesCreateRequest;
import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import com.codeit.weatherwear.domain.clothes.service.ClothesService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clothes")
public class ClothesController implements ClothesApi {

    private final ClothesService clothesService;

    @Override
    @PostMapping
    public ResponseEntity<ClothesDto> create(@Valid @RequestBody ClothesCreateRequest request) {
        ClothesDto createClothes = clothesService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createClothes);
    }

    @Override
    @DeleteMapping("/{clothesId}")
    public ResponseEntity<Void> delete(@PathVariable UUID clothesId) {
        clothesService.delete(clothesId);
        return ResponseEntity.noContent().build();
    }


}
