package com.codeit.weatherwear.global.event;

import com.codeit.weatherwear.global.event.dto.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringEventPublisher implements DomainEventPublisher{
  private final ApplicationEventPublisher delegate;

  @Override
  public <E extends DomainEvent> void publish(E event) {
    delegate.publishEvent(event);
  }
}
