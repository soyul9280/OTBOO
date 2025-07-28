package com.codeit.weatherwear.global.sse;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class SseEmitterRepositoryTest {

  SseEmitterRepository sseEmitterRepository;

  @BeforeEach
  void setUp() {
    sseEmitterRepository = new SseEmitterRepository();
  }

  @Test
  void save() {
    UUID receiverId = UUID.randomUUID();
    SseEmitter sseEmitter = new SseEmitter(1000L);

    assertThat(sseEmitterRepository.findByReceiverId(receiverId)).isEmpty();
    sseEmitterRepository.save(receiverId, sseEmitter);

    assertThat(sseEmitterRepository.findByReceiverId(receiverId))
        .hasSize(1)
        .first()
        .isEqualTo(sseEmitter);
  }

  @Test
  void delete() {
    UUID receiverId = UUID.randomUUID();
    SseEmitter sseEmitter = new SseEmitter(1000L);

    sseEmitterRepository.save(receiverId, sseEmitter);
    sseEmitterRepository.delete(receiverId, sseEmitter);

    assertThat(sseEmitterRepository.findByReceiverId(receiverId)).isEmpty();
  }

}