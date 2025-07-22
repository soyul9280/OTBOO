package com.codeit.weatherwear.domain.weather.controller.api;

import com.codeit.weatherwear.domain.location.dto.LocationDto;
import com.codeit.weatherwear.domain.weather.dto.response.WeatherDto;
import com.codeit.weatherwear.global.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "날씨 관리", description = "날씨 관련 API")
@RequestMapping("/api/weathers")
public interface WeatherApi {

  @Operation(summary = "날씨 정보 조회", description = "날씨 정보 조회 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "날씨 조회 성공",
          content = @Content(schema = @Schema(implementation = List.class))),
      @ApiResponse(
          responseCode = "400",
          description = "날씨 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @GetMapping
  ResponseEntity<List<WeatherDto>> getWeatherInfo(
      @RequestParam(name = "latitude") double latitude,
      @RequestParam(name = "longitude") double longitude
  );

  @Operation(summary = "날씨 위치 정보 조회", description = "날씨 위치 정보 조회 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "날씨 위치 정보 조회 성공",
          content = @Content(schema = @Schema(implementation = LocationDto.class))),
      @ApiResponse(
          responseCode = "400",
          description = "날씨 위치 정보 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @GetMapping("/location")
  ResponseEntity<LocationDto> getLocationInfo(
      @RequestParam(name = "latitude") double latitude,
      @RequestParam(name = "longitude") double longitude
  );

}
