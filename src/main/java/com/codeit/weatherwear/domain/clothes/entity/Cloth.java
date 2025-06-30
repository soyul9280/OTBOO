package com.codeit.weatherwear.domain.clothes.entity;

import com.codeit.weatherwear.domain.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "clothes")
@Getter
@AllArgsConstructor
public class Cloth {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false)
    private UUID id;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(updatable = true)
    private Instant updatedAt;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type",nullable = false)
    private ClothType clothType;

    @Column(name = "image_url")
    private String clothesImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User user;

    @OneToMany(mappedBy = "cloth",cascade = CascadeType.ALL)
    @Builder.Default
    private List<ClothWithAttributes> clothesWithAttributes=new ArrayList<>();

    public void addAttribute(ClothWithAttributes attributes) {
        if (this.clothesWithAttributes == null) {
            this.clothesWithAttributes = new ArrayList<>();
        }
        this.clothesWithAttributes.add(attributes);
        attributes.setClothes(this);
    }
}
