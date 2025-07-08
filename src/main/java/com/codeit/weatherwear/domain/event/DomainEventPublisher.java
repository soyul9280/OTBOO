package com.codeit.weatherwear.domain.event;

import com.codeit.weatherwear.domain.event.dto.DomainEvent;

public interface DomainEventPublisher {

  <E extends DomainEvent> void publish(E event);
}
