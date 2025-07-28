package com.codeit.weatherwear.domain.auth.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.weatherwear.domain.auth.dto.ResetPasswordRequest;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.exception.UserNotFoundException;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

  @InjectMocks
  private AuthServiceImpl authService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private EmailService emailService;
  @Spy
  private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
  @Spy
  private SecureRandom secureRandom = new SecureRandom();

  UUID userId;
  User user;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    user = User.builder()
        .id(userId)
        .name("testname")
        .email("test@test.mail")
        .password(passwordEncoder.encode("testpw"))
        .build();
  }

  @Test
  void 비밀번호_초기화_성공() {
    // given
    ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest("test@test.mail");
    when(userRepository.findByEmail("test@test.mail")).thenReturn(Optional.of(user));
    doNothing().when(emailService).sendTempPasswordEmail(any(), any());

    // when
    authService.resetPassword(resetPasswordRequest);

    // then
    verify(emailService).sendTempPasswordEmail(any(), any());
    assertTrue(!passwordEncoder.matches("testpw", user.getPassword()));
    assertNotNull(user.getTempPasswordExpirationTime());
  }

  @Test
  void 비밀번호_초기화_실패() {
    // given
    ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest("test@test.mail");
    when(userRepository.findByEmail("test@test.mail")).thenReturn(Optional.empty());

    // when & then
    assertThrows(UserNotFoundException.class,
        () -> authService.resetPassword(resetPasswordRequest));
  }
}