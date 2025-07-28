package com.codeit.weatherwear.domain.directmessage.controller.api;


import com.codeit.weatherwear.domain.directmessage.dto.DirectMessageDto;
import com.codeit.weatherwear.domain.directmessage.dto.request.DirectMessageSearchRequest;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "DirectMessage", description = "DirectMessage API")
@RequestMapping("/api/direct-messages")
public interface DirectMessageApi {

  @Operation(summary = "DM 목록 조회", description = "DM 목록 조회 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "DM 목록 조회 성공"
      ),
      @ApiResponse(
          responseCode = "400",
          description = "DM 목록 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @GetMapping
  ResponseEntity<PageResponse<DirectMessageDto>> getDirectMessages(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @ModelAttribute @Valid DirectMessageSearchRequest directMessageSearchRequest
  );

}
