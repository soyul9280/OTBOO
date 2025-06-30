package com.codeit.weatherwear.domain.clothes.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.weatherwear.domain.clothes.entity.Attribute;
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
class AttributeRepositoryTest {

    @Autowired
    private AttributeRepository sut;

    @Test
    @DisplayName("저장 성공")
    void save_success() {
        //given
        Attribute attributes = Attribute.builder()
            .name("색상")
            .selectableValues(List.of("빨강", "파랑")).build();
        //when
        Attribute result = sut.save(attributes);
        //then
        assertThat(result.getName()).isEqualTo("색상");
        assertThat(result.getSelectableValues()).containsExactly("빨강", "파랑");
    }


}