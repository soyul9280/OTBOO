package com.codeit.weatherwear.domain.clothes.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.weatherwear.domain.clothes.entity.Attribute;
import com.codeit.weatherwear.global.config.JpaConfig;
import com.codeit.weatherwear.global.request.SortDirection;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import({JpaConfig.class})
class AttributeRepositoryTest {

    @Autowired
    private AttributeRepository sut;

    Attribute thick;
    Attribute size;
    Attribute touch;

    @BeforeEach
    void setUp() {
        thick = Attribute.builder()
            .name("두께")
            .selectableValues(List.of("두꺼움", "얇음")).build();
        size = Attribute.builder()
            .name("사이즈")
            .selectableValues(List.of("S", "L")).build();
        touch = Attribute.builder()
            .name("촉감")
            .selectableValues(List.of("부드러움", "뻣뻣함")).build();
        sut.save(thick);
        sut.save(size);
        sut.save(touch);
    }

    @Test
    @DisplayName("저장 성공")
    void save_success() {
        //given
        Attribute attribute = Attribute.builder()
            .name("색상")
            .selectableValues(List.of("빨강", "파랑")).build();
        //when
        Attribute result = sut.save(attribute);
        //then
        assertThat(result.getName()).isEqualTo("색상");
        assertThat(result.getSelectableValues()).containsExactly("빨강", "파랑");
    }

    @Test
    @DisplayName("속성 조회 성공 - hasNext = false")
    void search_success() {
        //given
        int limit = 20;

        //when
        Slice<Attribute> attributeList = sut.searchAttributes(null, null, limit, "name",
            SortDirection.DESCENDING, null);
        List<Attribute> content = attributeList.getContent();

        //then
        assertThat(attributeList.hasNext()).isFalse();
        assertThat(content).hasSize(3);
        assertThat(content.get(0)).isEqualTo(touch);
        assertThat(content.get(1)).isEqualTo(size);
        assertThat(content.get(2)).isEqualTo(thick);
    }

    @Test
    @DisplayName("속성 조회 성공 - hasNext=true")
    void search_success_hasNext() {
        //given
        int limit = 2;

        //when
        Slice<Attribute> attributeList = sut.searchAttributes(null, null, limit, "name",
            SortDirection.DESCENDING, null);
        List<Attribute> content = attributeList.getContent();

        //then
        assertThat(attributeList.hasNext()).isTrue();
        assertThat(content).hasSize(2);
        assertThat(content.get(0)).isEqualTo(touch);
        assertThat(content.get(1)).isEqualTo(size);
    }

    @Test
    @DisplayName("속성 조회 성공 - idAfter존재")
    void search_success_cursor() {
        //given
        int limit = 2;
        UUID idAfter = thick.getId();

        //when
        Slice<Attribute> attributeList = sut.searchAttributes(null, idAfter, limit, "name",
            SortDirection.DESCENDING, null);
        List<Attribute> content = attributeList.getContent();

        //then
        assertThat(attributeList.hasNext()).isTrue();
        assertThat(content).hasSize(2);
        assertThat(content.get(0)).isEqualTo(touch);
        assertThat(content.get(1)).isEqualTo(size);
    }
}