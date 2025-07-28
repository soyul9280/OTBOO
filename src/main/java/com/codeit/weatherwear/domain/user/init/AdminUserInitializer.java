package com.codeit.weatherwear.domain.user.init;

import com.codeit.weatherwear.domain.user.entity.Role;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 사용자 관리 - 어드민 기능 서버 시작 시 어드민 계정 자동 초기화 email: admin@mail.com name: admin password: admin
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminUserInitializer implements ApplicationRunner {

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;
  @Value("${app.admin.name}")
  private String adminName;
  @Value("${app.admin.email}")
  private String adminEmail;
  @Value("${app.admin.password}")
  private String adminPassword;

  @Override
  public void run(ApplicationArguments args) throws Exception {

    if (!userRepository.existsByEmail(adminEmail) && !userRepository.existsByName(adminName)) {
      userRepository.save(
          User.builder()
              .name(adminName)
              .email(adminEmail)
              .password(passwordEncoder.encode(adminPassword))
              .role(Role.ADMIN)
              .build()
      );
      log.info("Admin User Created: {}", adminEmail);
    } else {
      log.info("Admin User({}) Already Exists", adminEmail);
    }
  }
}
