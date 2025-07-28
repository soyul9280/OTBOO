package com.codeit.weatherwear.domain.auth.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

  private final JavaMailSender javaMailSender;

  private final TemplateEngine templateEngine;

  @Value("${resetpassword.validity-seconds}")
  private long passwordValiditySeconds;

  @Async
  public void sendTempPasswordEmail(String toEmail, String tempPassword) {
    try {
      // Thymeleaf context 설정
      Context context = new Context();
      context.setVariable("tempPassword", tempPassword);
      context.setVariable("validMinutes", passwordValiditySeconds / 60);

      String htmlContent = templateEngine.process("email/reset-password", context);

      MimeMessage mimeMessage = javaMailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
      helper.setTo(toEmail);
      helper.setSubject("[옷장을 부탁해] 임시 비밀번호 안내");
      helper.setText(htmlContent, true); // HTML 본문

      javaMailSender.send(mimeMessage);
    } catch (Exception e) {
      log.error("Fail to send reset-password email", e.getMessage());
    }
  }
}