package com.codeit.weatherwear.global.sse;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class SseEmitterRepository {

  private final Map<UUID, List<SseEmitter>> data = new ConcurrentHashMap<>(100);

  public SseEmitter save(UUID receiverId, SseEmitter sseEmitter) {
    data.computeIfAbsent(receiverId, k -> new CopyOnWriteArrayList<>()).add(sseEmitter);
    return sseEmitter;
  }

  public List<SseEmitter> findByReceiverId(UUID receiverId) {
    return data.getOrDefault(receiverId, Collections.emptyList());
  }

  public List<SseEmitter> findAll() {
    return data.values().stream()
        .flatMap(List::stream)
        .toList();
  }

  public void delete(UUID receiverId, SseEmitter sseEmitter) {
    List<SseEmitter> sseEmitters = data.get(receiverId);
    if (sseEmitters != null) {
      sseEmitters.remove(sseEmitter);
      if (sseEmitters.isEmpty()) {
        data.remove(receiverId);
        sseEmitter.complete();
      }
    }
  }
}
