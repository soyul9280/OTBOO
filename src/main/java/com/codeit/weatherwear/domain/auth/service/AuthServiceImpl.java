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
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JavaMailSender javaMailSender;
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
    sendTempPasswordEmail(email, tempPassword);

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

  private void sendTempPasswordEmail(String toEmail, String tempPassword) {
    String content = """
        안녕하세요.
        "옷장을 부탁해" 서비스입니다.
                
        요청하신 임시 비밀번호는 아래와 같습니다.
        --------------
        %s
        --------------
                
        임시 비밀번호는 발급 시점으로부터 10분 간 유효합니다.
        로그인 후 반드시 비밀번호를 변경해주세요.
                
        감사합니다.        
        """.formatted(tempPassword);

    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(toEmail);
    message.setSubject("[옷장을 부탁해] 임시 비밀번호 안내");
    message.setText(content);

    javaMailSender.send(message);
  }

}
