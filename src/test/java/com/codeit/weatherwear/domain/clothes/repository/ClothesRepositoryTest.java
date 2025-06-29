package com.codeit.weatherwear.domain.clothes.repository;


import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.weatherwear.domain.clothes.entity.Attributes;
import com.codeit.weatherwear.domain.clothes.entity.Clothes;
import com.codeit.weatherwear.domain.clothes.entity.ClothesType;
import com.codeit.weatherwear.domain.clothes.entity.ClothesWithAttributes;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import com.codeit.weatherwear.global.config.JpaConfig;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import({JpaConfig.class})
public class ClothesRepositoryTest {

    @Autowired
    private ClothesRepository sut;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttributesRepository attributesRepository;

    @Test
    @DisplayName("저장 성공")
    void save_success() {
        // given
        User user = userRepository.save(User.builder()
            .name("테스트유저")
            .build());

        Attributes color = attributesRepository.save(Attributes.builder()
            .name("색상")
            .selectableValues(List.of("빨강", "파랑"))
            .build());

        Clothes clothes = Clothes.builder()
            .name("후드티")
            .clothesType(ClothesType.TOP)
            .user(user)
            .build();

        ClothesWithAttributes attr = ClothesWithAttributes.builder()
            .value("파랑")
            .attributes(color)
            .build();

        clothes.addAttributes(attr);

        // when
        Clothes saved = sut.save(clothes);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("후드티");
        assertThat(saved.getClothesWithAttributes()).hasSize(1);
        assertThat(saved.getClothesWithAttributes().get(0).getValue()).isEqualTo("파랑");
        assertThat(saved.getUser().getId()).isEqualTo(user.getId());
    }
}
