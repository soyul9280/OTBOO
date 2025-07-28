package com.codeit.weatherwear.global.event;

import com.codeit.weatherwear.global.event.dto.DomainEvent;

public interface DomainEventPublisher {

  <E extends DomainEvent> void publish(E event);
}
