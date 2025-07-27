package com.codeit.weatherwear.domain.clothes.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codeit.weatherwear.domain.clothes.entity.Attribute;
import com.codeit.weatherwear.global.config.ContainerInitializer;
import com.codeit.weatherwear.global.config.JpaConfig;
import com.codeit.weatherwear.global.request.SortDirection;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@ActiveProfiles("test")
@Import({JpaConfig.class})
@ContextConfiguration(initializers = ContainerInitializer.class)
class AttributeRepositoryTest {

  @Autowired
  private AttributeRepository sut;

  Attribute thick;
  Attribute size;
  Attribute touch;

  @BeforeEach
  void setUp() {
    sut.deleteAll();
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
  @DisplayName("existsByName - 이름으로 존재 여부 확인 성공")
  void existsByName_success() {
    // when
    boolean exists = sut.existsByName("사이즈");

    // then
    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("existsByName - 이름으로 존재 여부 확인 실패")
  void existsByName_fail() {
    // when
    boolean exists = sut.existsByName("색상");

    // then
    assertThat(exists).isFalse();
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
  @DisplayName("속성 조회 성공 - 키워드 검색 적용")
  void search_withKeyword() {
    // given
    int limit = 10;

    // when
    Slice<Attribute> result = sut.searchAttributes(null, null, limit, "name",
        SortDirection.ASCENDING, "사");

    // then
    List<Attribute> content = result.getContent();
    assertThat(content).extracting(Attribute::getName).allMatch(name -> name.contains("사"));
  }

  @Test
  @DisplayName("속성 조회 성공 - createdAt 정렬 기준")
  void search_createdAt_success() {
    // given
    int limit = 3;

    // when
    Slice<Attribute> attributes = sut.searchAttributes(null, null, limit, "createdAt",
        SortDirection.ASCENDING, null);

    // then
    List<Attribute> content = attributes.getContent();
    assertThat(content).hasSize(3);
    assertThat(content.get(0).getCreatedAt()).isBeforeOrEqualTo(content.get(1).getCreatedAt());
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

  @Test
  @DisplayName("속성 조회 실패 - 잘못된 정렬 기준")
  void search_fail_invalidSortBy() {
    // expect
    assertThatThrownBy(() ->
        sut.searchAttributes(null, null, 10, "invalidField", SortDirection.ASCENDING, null))
        //IllegalArgumentException->JPA의해 InvalidDataAccessApiUsageException감싸짐
        .isInstanceOf(InvalidDataAccessApiUsageException.class)
        .hasMessageContaining("지원하지 않는 정렬 기준입니다.");
  }

}