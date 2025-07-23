package com.codeit.weatherwear.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.kafka.topics")
public record KafkaTopics(
    String newFollower,
    String clothAttributeAdded,
    String clothAttributeUpdated,
    String directMessageReceived,
    String feedLike,
    String newFeedComment,
    String followeeFeedPosted,
    String roleChanged
) {

}
