package com.codeit.weatherwear.global.event.dto;

public sealed interface DomainEvent
    permits ClothAttributeAddedEvent,
    DirectMessageReceivedEvent,
    FeedLikeEvent,
    FolloweeFeedPostedEvent,
    NewFeedCommentEvent,
    NewFollowerEvent,
    RoleChangedEvent,
    MultipleNotificationCreatedEvent,
    NotificationCreatedEvent { }