package com.codeit.weatherwear.domain.auth.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

  @Mock
  private JavaMailSender javaMailSender;

  @Mock
  private TemplateEngine templateEngine;

  @InjectMocks
  private EmailServiceImpl emailService;

  @Test
  @DisplayName("비밀번호 초기화 이메일을 전송한다.")
  void sendTempPasswordEmail_shouldSendEmail() throws Exception {
    // given
    String toEmail = "test@example.com";
    String tempPassword = "abc123";

    // Thymeleaf 템플릿은 mocking
    String html = "<html><body>Your temp password: abc123</body></html>";
    when(templateEngine.process(eq("email/reset-password"),
        any(Context.class))).thenReturn(html);

    // MimeMessage mocking
    MimeMessage mimeMessage = mock(MimeMessage.class);
    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
    when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

    // when
    emailService.sendTempPasswordEmail(toEmail, tempPassword);

    // then
    verify(javaMailSender).send(mimeMessage); // 이메일을 전송했는가
    verify(templateEngine).process(eq("email/reset-password"),
        any(Context.class));  // "email/reset-password" 템플릿을 사용했는가
  }
}
