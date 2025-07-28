package com.codeit.weatherwear.global.event.dto;

public record ClothAttributeAddedEvent(
    String attributeName
) implements DomainEvent {
}
