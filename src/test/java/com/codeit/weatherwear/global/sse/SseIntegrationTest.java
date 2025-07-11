package com.codeit.weatherwear.global.sse;

import com.codeit.weatherwear.domain.notification.Notification.Level;
import com.codeit.weatherwear.domain.notification.NotificationDto;
import com.codeit.weatherwear.global.config.JpaConfig;
import com.codeit.weatherwear.global.config.TestSecurityConfig;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Import({JpaConfig.class, TestSecurityConfig.class})
class SseIntegrationTest {

  @Autowired
  WebTestClient webTestClient;
  @Autowired
  SseService sseService;

  @Test
  void sseConnect() {
    Flux<ServerSentEvent<NotificationDto>> responseBody = webTestClient.get()
        .uri("/api/sse")
        .accept(MediaType.TEXT_EVENT_STREAM)
        .headers(h -> h.setBasicAuth("user", "password"))
        .exchange()
        .expectStatus().isOk()
        .returnResult(new ParameterizedTypeReference<ServerSentEvent<NotificationDto>>() {})
        .getResponseBody();

    UUID userId = UUID.randomUUID();
    NotificationDto notificationDto = new NotificationDto(
        UUID.randomUUID(),
        Instant.now(),
        userId,
        "test event",
        "test event content",
        Level.INFO
    );

    StepVerifier.create(responseBody)
        .then(() -> sseService.send(userId, notificationDto))
        .expectNextMatches(e ->
            e.event().equals("notifications") && e.data().equals(notificationDto))
        .thenCancel()
        .verify();
  }
}