package com.codeit.weatherwear.domain.clothes.controller.api;

import com.codeit.weatherwear.domain.clothes.dto.request.ClothesCreateRequest;
import com.codeit.weatherwear.domain.clothes.dto.request.ClothesSearchRequest;
import com.codeit.weatherwear.domain.clothes.dto.request.ClothesUpdateRequest;
import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import com.codeit.weatherwear.global.response.ErrorResponse;
import com.codeit.weatherwear.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "의상 관리", description = "의상 관련 API")
@RequestMapping("/api/clothes")
public interface ClothApi {

    @Operation(summary = "옷 등록", description = "옷 등록 API")
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "옷 등록 성공",
            content = @Content(schema = @Schema(implementation = ClothesDto.class))),
        @ApiResponse(
            responseCode = "400",
            description = "옷 등록 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PostMapping
    ResponseEntity<ClothesDto> create(
        @RequestPart("request") ClothesCreateRequest request,
        @RequestPart(value="image",required = false)MultipartFile image);



    @Operation(summary = "옷 목록 조회", description = "옷 목록 조회 API")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "옷 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = ClothesDto.class))),
        @ApiResponse(
            responseCode = "400",
            description = "옷 목록 조회 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @GetMapping
    ResponseEntity<PageResponse<ClothesDto>> searchClothes(
        @ParameterObject ClothesSearchRequest request);




    @Operation(summary = "옷 수정", description = "옷 수정 API")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "옷 수정 성공",
            content = @Content(schema = @Schema(implementation = ClothesDto.class))),
        @ApiResponse(
            responseCode = "400",
            description = "옷 수정 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PatchMapping("/{clothesId}")
    ResponseEntity<ClothesDto> update(
        @Parameter(
            name="clothesId",
            required = true
        )
        @PathVariable(value = "clothesId") UUID clothesId,
        @RequestPart("request") ClothesUpdateRequest request,
        @RequestPart(value="image",required = false)MultipartFile image);


    @Operation(summary = "옷 삭제", description = "옷 삭제 API")
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "옷 삭제 성공"),
        @ApiResponse(
            responseCode = "400",
            description = "옷 삭제 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @DeleteMapping("/{clothesId}")
    ResponseEntity<Void> delete(
        @Parameter(
            name="clothesId",
            required = true
        )
        @PathVariable(value = "clothesId") UUID clothesId);
}

