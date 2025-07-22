package com.codeit.weatherwear.domain.notification.controller.api;

import com.codeit.weatherwear.domain.notification.dto.NotificationDto;
import com.codeit.weatherwear.domain.notification.dto.request.NotificationSearchRequest;
import com.codeit.weatherwear.domain.security.customauthentication.CustomUserDetails;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "알림", description = "알림 API")
@RequestMapping("/api/notifications")
public interface NotificationApi {

  @Operation(summary = "알림 목록 조회", description = "알림 목록 조회 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "알림 목록 조회 성공"
      ),
      @ApiResponse(
          responseCode = "400",
          description = "알림 목록 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @GetMapping
  ResponseEntity<PageResponse<NotificationDto>> getNotifications(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @ModelAttribute @Valid NotificationSearchRequest notificationSearchRequest);

  @Operation(summary = "알림 읽음 처리", description = "알림 읽음 처리 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "204",
          description = "알림 읽음 처리 성공"
      )
  })
  @DeleteMapping("/{id}")
  ResponseEntity<Void> deleteNotification(@PathVariable UUID id);
}
