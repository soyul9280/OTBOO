package com.codeit.weatherwear.domain.user.dto.response;

import com.codeit.weatherwear.domain.location.dto.LocationDto;
import com.codeit.weatherwear.domain.user.entity.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "사용자 프로필 정보")
public class ProfileDto {

  private final UUID userId;
  private final String name;
  private final Gender gender;
  private final LocalDate birthDate;
  private final LocationDto location;
  private final Integer temperatureSensitivity;
  private final String profileImageUrl;
}
