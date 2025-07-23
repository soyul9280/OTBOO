package com.codeit.weatherwear.global.event.dto;

public record ClothAttributeUpdatedEvent(
    String attributeName
) implements DomainEvent {

}
