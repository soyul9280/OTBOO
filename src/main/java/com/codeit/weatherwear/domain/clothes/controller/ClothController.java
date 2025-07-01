package com.codeit.weatherwear.domain.clothes.controller;

import com.codeit.weatherwear.domain.clothes.controller.api.ClothApi;
import com.codeit.weatherwear.domain.clothes.dto.request.ClothesCreateRequest;
import com.codeit.weatherwear.domain.clothes.dto.request.ClothesSearchRequest;
import com.codeit.weatherwear.domain.clothes.dto.request.ClothesUpdateRequest;
import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import com.codeit.weatherwear.domain.clothes.service.ClothService;
import com.codeit.weatherwear.global.response.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clothes")
public class ClothController implements ClothApi {

    private final ClothService clothService;

    /**
     * 의상을 등록합니다.
     *
     * @param request 등록 의상 정보
     * @return
     */
    @Override
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ClothesDto> create(
        @Valid @RequestPart("request") ClothesCreateRequest request,
        @RequestPart(value="image",required = false) MultipartFile image) {
        ClothesDto createClothes = clothService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createClothes);
    }

    /**
     * 의상을 조회합니다.
     *
     * @param request 조회조건
     * @return
     */
    @Override
    public ResponseEntity<PageResponse<ClothesDto>> searchClothes(
        @ModelAttribute @Valid ClothesSearchRequest request) {
        PageResponse<ClothesDto> result = clothService.searchClothes(request);
        return ResponseEntity.ok(result);
    }


    /**
     * 의상을 수정합니다.
     *
     * @param clothesId 수정 요청 ID
     * @param request 수정 요청 DTO
     * @param image 수정 요청 이미지
     * @return
     */
    @Override
    @PatchMapping(
        value = "/{clothesId}"
        ,consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ClothesDto> update(
        @PathVariable("clothesId") UUID clothesId,
        @Valid @RequestPart("request") ClothesUpdateRequest request,
        @RequestPart(value = "image",required = false) MultipartFile image) {
        ClothesDto update = clothService.update(clothesId, request);
        return ResponseEntity.ok(update);
    }

    /**
     * 의상을 삭제합니다.
     *
     * @param clothesId 의상 ID
     * @return
     */
    @Override
    @DeleteMapping("/{clothesId}")
    public ResponseEntity<Void> delete(@PathVariable UUID clothesId) {
        clothService.delete(clothesId);
        return ResponseEntity.noContent().build();
    }


}
