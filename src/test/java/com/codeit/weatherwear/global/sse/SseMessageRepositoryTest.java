package com.codeit.weatherwear.global.sse;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.weatherwear.domain.notification.Notification.Level;
import com.codeit.weatherwear.domain.notification.dto.NotificationDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SseMessageRepositoryTest {

  SseMessageRepository repo;

  UUID receiverId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    repo = new SseMessageRepository();
    ReflectionTestUtils.setField(repo, "eventQueueCapacity", 2);
  }

  @Test
  void save() {
    SseMessage sseMessage = repo.save(SseMessage.create(receiverId, dummy()));

    ConcurrentLinkedDeque<UUID> eventIdQueue = (ConcurrentLinkedDeque<UUID>) ReflectionTestUtils
        .getField(repo, "eventIdQueue");
    ConcurrentHashMap<UUID, SseMessage> messages = (ConcurrentHashMap<UUID, SseMessage>) ReflectionTestUtils
        .getField(repo, "messages");

    assertThat(eventIdQueue).containsExactly(sseMessage.getEventId());
    assertThat(messages).containsEntry(sseMessage.getEventId(), sseMessage);
  }

  @Test
  void saveWhenEventQueueCapacityIsFull() {
    SseMessage sseMessage1 = repo.save(SseMessage.create(receiverId, dummy()));
    SseMessage sseMessage2 = repo.save(SseMessage.create(receiverId, dummy()));
    SseMessage sseMessage3 = repo.save(SseMessage.create(receiverId, dummy()));
    SseMessage sseMessage4 = repo.save(SseMessage.create(receiverId, dummy()));

    ConcurrentLinkedDeque<UUID> eventIdQueue = (ConcurrentLinkedDeque<UUID>) ReflectionTestUtils
        .getField(repo, "eventIdQueue");
    assertThat(eventIdQueue).containsExactly(sseMessage3.getEventId(), sseMessage4.getEventId());

  }

  @Test
  void saveMultiThread() throws InterruptedException {
    int threadCount = 100;
    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

    CountDownLatch latch = new CountDownLatch(threadCount);

    for (int i = 0; i < threadCount; i++) {
      executorService.submit(() -> {
        repo.save(SseMessage.create(receiverId, dummy()));
      });
    }
    latch.await(2, TimeUnit.SECONDS);

    ConcurrentLinkedDeque<UUID> eventIdQueue = (ConcurrentLinkedDeque<UUID>) ReflectionTestUtils
        .getField(repo, "eventIdQueue");

    assertThat(eventIdQueue).hasSize(2);
  }

  @Test
  void find() {
    SseMessage sseMessage1 = repo.save(SseMessage.create(receiverId, dummy()));
    SseMessage sseMessage2 = repo.save(SseMessage.create(receiverId, dummy()));

    List<SseMessage> messages = repo
        .findAllByEventIdAfterAndReceiverId(sseMessage1.getEventId(), receiverId);

    assertThat(messages).hasSize(1);
    assertThat(messages.get(0)).isEqualTo(sseMessage2);

    messages = repo.findAllByEventIdAfterAndReceiverId(sseMessage2.getEventId(), receiverId);

    assertThat(messages).isEmpty();
  }

  private NotificationDto dummy() {
    return new NotificationDto(
        UUID.randomUUID(), Instant.now(), receiverId, "dummy", "dummy dto", Level.INFO);
  }
}