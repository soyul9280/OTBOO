package com.codeit.weatherwear.global.event.dto;

public sealed interface DomainEvent
    permits ClothAttributeAddedEvent,
    ClothAttributeUpdatedEvent,
    DirectMessageReceivedEvent,
    FeedLikeEvent,
    FolloweeFeedPostedEvent,
    NewFeedCommentEvent,
    NewFollowerEvent,
    RoleChangedEvent,
    MultipleNotificationCreatedEvent,
    NotificationCreatedEvent,
    WeatherAlertEvent {}