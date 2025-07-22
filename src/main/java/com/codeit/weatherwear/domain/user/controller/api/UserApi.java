package com.codeit.weatherwear.domain.user.controller.api;

import com.codeit.weatherwear.domain.user.dto.request.ChangePasswordRequest;
import com.codeit.weatherwear.domain.user.dto.request.ProfileUpdateRequest;
import com.codeit.weatherwear.domain.user.dto.request.UserCreateRequest;
import com.codeit.weatherwear.domain.user.dto.request.UserLockUpdateRequest;
import com.codeit.weatherwear.domain.user.dto.request.UserRoleUpdateRequest;
import com.codeit.weatherwear.domain.user.dto.request.UserSearchRequest;
import com.codeit.weatherwear.domain.user.dto.response.ProfileDto;
import com.codeit.weatherwear.domain.user.dto.response.UserDto;
import com.codeit.weatherwear.global.response.ErrorResponse;
import com.codeit.weatherwear.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "프로필 관리", description = "프로필 관련 API")
@RequestMapping("/api/users")
public interface UserApi {

  @Operation(summary = "사용자 등록(회원가입)", description = "새로운 사용자를 등록합니다.")
  @ApiResponses({
      @ApiResponse(
          responseCode = "201",
          description = "사용자 등록(회원가입) 성공",
          content = @Content(schema = @Schema(implementation = UserDto.class))),
      @ApiResponse(
          responseCode = "400",
          description = "사용자 등록(회원가입) 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @PostMapping
  ResponseEntity<UserDto> createUser(@RequestBody UserCreateRequest userCreateRequest);

  @Operation(summary = "프로필 조회", description = "특정 사용자의 프로필을 조회합니다.")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "프로필 조회 성공",
          content = @Content(schema = @Schema(implementation = ProfileDto.class))),
      @ApiResponse(
          responseCode = "404",
          description = "프로필 조회 실패 (사용자 없음)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @GetMapping
  ResponseEntity<ProfileDto> findProfile(@PathVariable UUID userId);

  @Operation(summary = "계정 목록 조회", description = "사용자 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "계정 목록 조회 성공"
      ),
      @ApiResponse(
          responseCode = "400",
          description = "계정 목록 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @GetMapping
  ResponseEntity<PageResponse<UserDto>> searchUsers(
      @ModelAttribute @Valid UserSearchRequest userSearchRequest);

  @Operation(summary = "프로필 업데이트", description = "사용자의 프로필 정보와 프로필 이미지를 업데이트합니다.")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "프로필 업데이트 성공",
          content = @Content(schema = @Schema(implementation = ProfileDto.class))),
      @ApiResponse(
          responseCode = "404",
          description = "프로필 업데이트 실패 (사용자 없음)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @PatchMapping
  ResponseEntity<ProfileDto> updateProfile(
      @PathVariable UUID userId,
      @Valid @RequestPart("request") ProfileUpdateRequest profileUpdateRequest,
      @RequestPart(value = "image", required = false) MultipartFile profileImage);

  @Operation(summary = "계정 잠금 상태 변경", description = "[어드민 기능] 사용자 계정을 잠그거나 잠금 해제합니다.")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "계정 잠금 상태 변경 성공",
          content = @Content(schema = @Schema(implementation = UUID.class))),
      @ApiResponse(
          responseCode = "404",
          description = "계정 잠금 상태 변경 실패 (사용자 없음)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @PatchMapping
  ResponseEntity<UUID> updateLock(@PathVariable UUID userId,
      @RequestBody UserLockUpdateRequest userLockUpdateRequest);

  @Operation(summary = "비밀번호 변경", description = "비밀번호를 변경합니다.")
  @ApiResponses({
      @ApiResponse(
          responseCode = "204",
          description = "비밀번호 변경 성공",
          content = @Content(schema = @Schema())),
      @ApiResponse(
          responseCode = "404",
          description = "비밀번호 변경 실패 (사용자 없음)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @PatchMapping
  ResponseEntity<Void> updatePassword(@PathVariable UUID userId,
      @Valid @RequestBody ChangePasswordRequest changePasswordRequest);

  @Operation(summary = "권한 수정", description = "[어드민 기능] 사용자의 권한을 수정합니다.")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "권한 수정 성공",
          content = @Content(schema = @Schema(implementation = UserDto.class))),
      @ApiResponse(
          responseCode = "404",
          description = "권한 수정 실패 (사용자 없음)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @PatchMapping
  ResponseEntity<UserDto> updateRole(@PathVariable UUID userId,
      @RequestBody UserRoleUpdateRequest userRoleUpdateRequest);
}
