package com.codeit.weatherwear.domain.clothes.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.weatherwear.domain.clothes.entity.Attribute;
import com.codeit.weatherwear.domain.clothes.entity.Cloth;
import com.codeit.weatherwear.domain.clothes.entity.ClothType;
import com.codeit.weatherwear.domain.clothes.entity.ClothWithAttributes;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import com.codeit.weatherwear.global.config.JpaConfig;
import com.codeit.weatherwear.global.config.TestContainerConfig;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import({JpaConfig.class, TestContainerConfig.class})
public class ClothWithAttributesRepositoryTest {

  @Autowired
  private ClothWithAttributesRepository sut;

  @Autowired
  private AttributeRepository attributeRepository;

  @Autowired
  private ClothRepository clothRepository;

  @Autowired
  private UserRepository userRepository;

  private Attribute color;
  private Cloth cloth;

  @BeforeEach
  void setUp() {
    User user = userRepository.save(User.builder()
        .email("user@test.com")
        .name("user")
        .password("pass")
        .build());

    color = attributeRepository.save(Attribute.builder()
        .name("색상")
        .selectableValues(List.of("빨강", "파랑", "하양"))
        .build());

    cloth = Cloth.builder()
        .name("셔츠")
        .clothType(ClothType.TOP)
        .user(user)
        .build();

    cloth.addAttribute(ClothWithAttributes.builder()
        .value("빨강")
        .attribute(color)
        .build());

    cloth.addAttribute(ClothWithAttributes.builder()
        .value("파랑")
        .attribute(color)
        .build());

    clothRepository.save(cloth);
  }

  @Test
  @DisplayName("속성에 사용된 값 목록 조회")
  void findUsedValuesByAttribute_success() {
    // when
    List<String> usedValues = sut.findUsedValuesByAttribute(color.getId());

    // then
    assertThat(usedValues).containsExactlyInAnyOrder("빨강", "파랑");
  }

  @Test
  @DisplayName("속성에 사용된 값이 없으면 빈 리스트 반환")
  void findUsedValuesByAttribute_empty() {
    // given
    Attribute newAttr = attributeRepository.save(Attribute.builder()
        .name("두께")
        .selectableValues(List.of("얇음", "두꺼움"))
        .build());

    // when
    List<String> result = sut.findUsedValuesByAttribute(newAttr.getId());

    // then
    assertThat(result).isEmpty();
  }
}