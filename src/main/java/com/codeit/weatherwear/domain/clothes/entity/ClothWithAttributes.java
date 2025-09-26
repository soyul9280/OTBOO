package com.codeit.weatherwear.domain.clothes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Table(name = "clothes_attribute")
@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClothWithAttributes {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @CreatedDate
  @Column(updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(updatable = true)
  private Instant updatedAt;

  @Column(name = "\"value\"")
  private String value;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "clothes_id")
  private Cloth cloth;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "definition_id")
  @BatchSize(size = 100)
  private Attribute attribute;

  @Builder
  public ClothWithAttributes(UUID id, Instant createdAt, Instant updatedAt, String value,
      Cloth cloth,
      Attribute attribute) {
    this.id = id;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.value = value;
    this.cloth = cloth;
    this.attribute = attribute;
  }

  protected void setClothes(Cloth cloth) {
    this.cloth = cloth;
  }
}

