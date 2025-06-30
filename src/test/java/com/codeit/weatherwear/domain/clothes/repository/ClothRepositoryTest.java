package com.codeit.weatherwear.domain.clothes.repository;


import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.weatherwear.domain.clothes.entity.Attribute;
import com.codeit.weatherwear.domain.clothes.entity.Cloth;
import com.codeit.weatherwear.domain.clothes.entity.ClothType;
import com.codeit.weatherwear.domain.clothes.entity.ClothWithAttributes;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import com.codeit.weatherwear.global.config.JpaConfig;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import({JpaConfig.class})
public class ClothRepositoryTest {

    @Autowired
    private ClothRepository sut;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttributeRepository attributeRepository;

    @Test
    @DisplayName("저장 성공")
    void save_success() {
        // given
        User user = userRepository.save(User.builder()
            .name("테스트유저")
            .build());

        Attribute color = attributeRepository.save(Attribute.builder()
            .name("색상")
            .selectableValues(List.of("빨강", "파랑"))
            .build());

        Cloth clothes = Cloth.builder()
            .name("후드티")
            .clothType(ClothType.TOP)
            .user(user)
            .build();

        ClothWithAttributes attr = ClothWithAttributes.builder()
            .value("파랑")
            .attribute(color)
            .build();

        clothes.addAttribute(attr);

        // when
        Cloth saved = sut.save(clothes);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("후드티");
        assertThat(saved.getClothesWithAttributes()).hasSize(1);
        assertThat(saved.getClothesWithAttributes().get(0).getValue()).isEqualTo("파랑");
        assertThat(saved.getUser().getId()).isEqualTo(user.getId());
    }
}
