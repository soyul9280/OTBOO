package com.codeit.weatherwear.global.sse;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class SseMessageRepository {

  private final ConcurrentLinkedDeque<UUID> eventIdQueue = new ConcurrentLinkedDeque<>();
  private final Map<UUID, SseMessage> messages = new ConcurrentHashMap<>();

  @Value("${sse.event-queue-capacity}")
  private int eventQueueCapacity;

  public SseMessage save(SseMessage message) {
    makeAvailableCapacity();

    eventIdQueue.addLast(message.getEventId());
    messages.put(message.getEventId(), message);
    return message;
  }

  public List<SseMessage> findAllByEventIdAfterAndReceiverId(UUID eventId, UUID receiverId) {
    return eventIdQueue.stream()
        .dropWhile(id -> !id.equals(eventId))
        .skip(1)
        .map(messages::get)
        .filter(sseMessage -> sseMessage.isReceivable(receiverId))
        .toList();
  }

  private void makeAvailableCapacity() {
    int availableCapacity = eventQueueCapacity - eventIdQueue.size();
    while (availableCapacity < 1) {
      messages.remove(eventIdQueue.removeFirst());
      availableCapacity++;
    }
  }
}
