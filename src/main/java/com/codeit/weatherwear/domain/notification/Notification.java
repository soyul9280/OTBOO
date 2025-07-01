package com.codeit.weatherwear.domain.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Notification {

  @Id
  @GeneratedValue
  private UUID id;

  @CreatedDate
  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private UUID receiverId;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String content;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Level level;

  public enum Level {
    INFO, WARNING, ERROR;
  }

  @Builder
  private Notification(UUID receiverId, String title, String content, Level level) {
    this.receiverId = receiverId;
    this.title = title;
    this.content = content;
    this.level = level;
  }

  public static Notification create(UUID receiverId, String title, String content, Level level) {
    return Notification.builder()
        .receiverId(receiverId)
        .title(title)
        .content(content)
        .level(level)
        .build();
  }
}
