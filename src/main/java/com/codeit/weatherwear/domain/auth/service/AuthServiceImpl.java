package com.codeit.weatherwear.domain.auth.service;

import com.codeit.weatherwear.domain.auth.dto.ResetPasswordRequest;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.exception.UserNotFoundException;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import java.security.SecureRandom;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final EmailService emailService;
  private final SecureRandom secureRandom = new SecureRandom();
  private final static String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz012345678";

  @Value("${resetpassword.validity-seconds}")
  private long PASSWORD_VALIDITY_SECONDS;

  // 비밀번호 초기화
  @Transactional
  @Override
  public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
    String email = resetPasswordRequest.email();

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UserNotFoundException());

    // 임시 비밀번호 발급
    String tempPassword = generateTempPassword();

    // 이메일 전송
    emailService.sendTempPasswordEmail(email, tempPassword);

    // DB 업데이트
    String encodedTempPassword = passwordEncoder.encode(tempPassword);
    user.setTempPassword(encodedTempPassword, Instant.now().plusSeconds(PASSWORD_VALIDITY_SECONDS));
  }

  // 임시 비밀번호 생성
  // 8자리, 영문, 숫자
  private String generateTempPassword() {
    StringBuffer stringBuffer = new StringBuffer();
    for (int i = 0; i < 8; i++) {
      // 무작위 인덱스 반환
      int index = secureRandom.nextInt(CHARS.length());
      stringBuffer.append(CHARS.charAt(index));
    }
    return stringBuffer.toString();
  }


}
