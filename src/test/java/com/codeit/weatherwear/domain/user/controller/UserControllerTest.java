package com.codeit.weatherwear.domain.user.controller;


import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.weatherwear.domain.user.dto.request.ChangePasswordRequest;
import com.codeit.weatherwear.domain.user.dto.request.ProfileUpdateRequest;
import com.codeit.weatherwear.domain.user.dto.request.UserCreateRequest;
import com.codeit.weatherwear.domain.user.dto.request.UserLockUpdateRequest;
import com.codeit.weatherwear.domain.user.dto.request.UserRoleUpdateRequest;
import com.codeit.weatherwear.domain.user.dto.response.ProfileDto;
import com.codeit.weatherwear.domain.user.dto.response.UserDto;
import com.codeit.weatherwear.domain.user.entity.Gender;
import com.codeit.weatherwear.domain.user.entity.Role;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.exception.UserAlreadyExistsException;
import com.codeit.weatherwear.domain.user.exception.UserNotFoundException;
import com.codeit.weatherwear.domain.user.service.UserService;
import com.codeit.weatherwear.global.base.BaseControllerTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(UserController.class)
class UserControllerTest extends BaseControllerTest {

  @MockitoBean
  private UserService userService;
  @Autowired
  private ObjectMapper objectMapper;

  UUID testUserId;
  User testUser;
  UserDto testUserDto;
  ProfileDto testProfileDto;

  @BeforeEach
  void setUp() {
    testUserId = UUID.randomUUID();
    testUser = User.builder()
        .id(testUserId)
        .name("test")
        .password("testpassword")
        .email("test@test.com")
        .locked(false)
        .build();
    testUserDto = new UserDto(testUser.getId(),
        testUser.getCreatedAt(),
        testUser.getEmail(),
        testUser.getName(),
        testUser.getRole(),
        testUser.getLinkedOAuthProviders(),
        testUser.isLocked());
    testProfileDto = new ProfileDto(
        testUser.getId(),
        testUser.getName(),
        testUser.getGender(),
        testUser.getBirthDate(),
        null,
        testUser.getTemperatureSensitivity(),
        testUser.getProfileImageUrl()
    );
  }

  @Test
  void 회원가입_성공() throws Exception {
    // given
    UserCreateRequest userCreateRequest = new UserCreateRequest("test", "test@test.com",
        "testpassword");

    given(userService.create(userCreateRequest)).willReturn(testUserDto);

    // when & then
    mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCreateRequest))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(testUserId.toString()))
        .andExpect(jsonPath("$.name").value("test"));
  }

  @Test
  void 회원가입_실패() throws Exception {
    // given
    UserCreateRequest userCreateRequest = new UserCreateRequest("test", "test@test.com",
        "testpassword");

    given(userService.create(userCreateRequest)).willThrow(new UserAlreadyExistsException());

    // when & then
    mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCreateRequest))
        )
        .andExpect(status().isBadRequest());
  }

  @Test
  void 프로필_조회_성공() throws Exception {
    // given
    given(userService.findProfile(testUserId)).willReturn(testProfileDto);

    // when & then

    mockMvc.perform(
            get("/api/users/{userId}/profiles", testUserId)
        ).andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(testUserId.toString()))
        .andExpect(jsonPath("$.name").value("test"));
  }

  @Test
  void 프로필_조회_실패() throws Exception {
    // given
    given(userService.findProfile(testUserId)).willThrow(new UserNotFoundException(testUserId));

    // when & then
    mockMvc.perform(
        get("/api/users/{userId}/profiles", testUserId)
    ).andExpect(status().isNotFound());
  }

  @Test
  void 프로필_수정_성공() throws Exception {
    // given
    ProfileUpdateRequest request = new ProfileUpdateRequest(
        "newName",
        Gender.MALE,
        LocalDate.now(),
        null,
        5
    );

    ProfileDto profileDto = new ProfileDto(
        testUserId,
        "newName",
        Gender.MALE,
        LocalDate.now(),
        null,
        5,
        null
    );

    given(userService.updateProfile(testUserId, request, null)).willReturn(profileDto);

    // when & then
    MockMultipartFile requestPart = new MockMultipartFile(
        "request",
        "request.json",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    mockMvc.perform(multipart("/api/users/{userId}/profiles", testUserId)
            .file(requestPart)
            .with(rq -> {
              rq.setMethod("PATCH");
              return rq;
            })
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(testUserId.toString()))
        .andExpect(jsonPath("$.name").value("newName"));
  }

  @Test
  void 프로필_수정_실패() throws Exception {
    // given
    ProfileUpdateRequest request = new ProfileUpdateRequest(
        "newName",
        Gender.MALE,
        LocalDate.now(),
        null,
        5
    );
    given(userService.updateProfile(testUserId, request, null)).willThrow(
        new UserNotFoundException(testUserId));

    // when & then
    MockMultipartFile requestPart = new MockMultipartFile(
        "request",
        "request.json",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    mockMvc.perform(multipart("/api/users/{userId}/profiles", testUserId)
            .file(requestPart)
            .with(rq -> {
              rq.setMethod("PATCH");
              return rq;
            })
        )
        .andExpect(status().isNotFound());
  }

  @Test
  void 잠금_상태_변경_성공() throws Exception {
    // given
    UserLockUpdateRequest userLockUpdateRequest = new UserLockUpdateRequest(true);
    given(userService.updateLock(testUserId, userLockUpdateRequest)).willReturn(testUserId);

    // when & then
    mockMvc.perform(
            patch("/api/users/{userId}/lock", testUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userLockUpdateRequest))
        ).andExpect(status().isOk())
        .andExpect(result -> result.equals(testUserId.toString()));
  }

  @Test
  void 잠금_상태_변경_실패() throws Exception {
    // given
    UserLockUpdateRequest userLockUpdateRequest = new UserLockUpdateRequest(true);
    given(userService.updateLock(testUserId, userLockUpdateRequest)).willThrow(
        new UserNotFoundException(testUserId));

    // when & then
    mockMvc.perform(
        patch("/api/users/{userId}/lock", testUserId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userLockUpdateRequest))
    ).andExpect(status().isNotFound());
  }

  @Test
  void 비밀번호_변경_성공() throws Exception {
    // given
    ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest("newPassword");
    willDoNothing().given(userService).updatePassword(testUserId, changePasswordRequest);

    // when & then
    mockMvc.perform(
        patch("/api/users/{userId}/password", testUserId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(changePasswordRequest))
    ).andExpect(status().isOk());
  }

  @Test
  void 비밀번호_변경_validation_실패() throws Exception {
    // given
    ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest("");
    willDoNothing().given(userService).updatePassword(testUserId, changePasswordRequest);

    // when & then
    mockMvc.perform(
        patch("/api/users/{userId}/password", testUserId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(changePasswordRequest))
    ).andExpect(status().isBadRequest());
  }

  @Test
  void 비밀번호_변경_사용자_검증_실패() throws Exception {
    // given
    ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest("newPassword");
    willThrow(new UserNotFoundException(testUserId)).given(userService)
        .updatePassword(testUserId, changePasswordRequest);

    // when & then
    mockMvc.perform(
        patch("/api/users/{userId}/password", testUserId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(changePasswordRequest))
    ).andExpect(status().isNotFound());
  }

  @Test
  void 권한_수정_성공() throws Exception {
    // given
    UserRoleUpdateRequest userRoleUpdateRequest = new UserRoleUpdateRequest(Role.ADMIN);

    UserDto userDto = new UserDto(
        testUser.getId(),
        testUser.getCreatedAt(),
        testUser.getEmail(),
        testUser.getName(),
        Role.ADMIN,
        testUser.getLinkedOAuthProviders(),
        testUser.isLocked()
    );

    given(userService.updateRole(testUserId, userRoleUpdateRequest)).willReturn(userDto);

    // when & then
    mockMvc.perform(
            patch("/api/users/{userId}/role", testUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRoleUpdateRequest))
        ).andExpect(status().isOk())
        .andExpect(jsonPath("$.role").value(Role.ADMIN.name()));
  }

  @Test
  void 권한_수정_실패() throws Exception {
    // given
    UserRoleUpdateRequest userRoleUpdateRequest = new UserRoleUpdateRequest(Role.ADMIN);

    given(userService.updateRole(testUserId, userRoleUpdateRequest)).willThrow(
        new UserNotFoundException(testUserId));

    // when & then
    mockMvc.perform(
        patch("/api/users/{userId}/role", testUserId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userRoleUpdateRequest))
    ).andExpect(status().isNotFound());
  }

}