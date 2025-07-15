package com.codeit.weatherwear.domain.clothes.repository;


import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.weatherwear.domain.clothes.entity.Attribute;
import com.codeit.weatherwear.domain.clothes.entity.Cloth;
import com.codeit.weatherwear.domain.clothes.entity.ClothType;
import com.codeit.weatherwear.domain.clothes.entity.ClothWithAttributes;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import com.codeit.weatherwear.global.config.JpaConfig;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
public class ClothRepositoryTest {

  @Autowired
  private ClothRepository sut;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private AttributeRepository attributeRepository;
  User user;
  Cloth thinScarf;
  Cloth whiteScarf;
  Cloth redScarf;
  Attribute color;
  Attribute thick;

  @BeforeEach
  void setUp() throws InterruptedException {
    user = User.builder()
        .email("user@test.com")
        .name("user")
        .password("user1234!")
        .build();
    userRepository.save(user);

    color = attributeRepository.save(Attribute.builder()
        .name("색상")
        .selectableValues(List.of("빨강", "파랑", "하양"))
        .build());

    thick = attributeRepository.save(Attribute.builder()
        .name("두께")
        .selectableValues(List.of("두꺼움", "얇음")).build());

    ClothWithAttributes white = ClothWithAttributes.builder()
        .value("하양")
        .attribute(color)
        .build();

    ClothWithAttributes red = ClothWithAttributes.builder()
        .value("빨강")
        .attribute(color)
        .build();

    ClothWithAttributes thin = ClothWithAttributes.builder()
        .value("얇음")
        .attribute(thick)
        .build();

    thinScarf = Cloth.builder()
        .name("얇은 스카프")
        .clothType(ClothType.SCARF)
        .user(user)
        .build();
    thinScarf.addAttribute(white);
    thinScarf.addAttribute(thin);

    whiteScarf = Cloth.builder()
        .name("하얀 스카프")
        .clothType(ClothType.SCARF)
        .user(user)
        .build();
    whiteScarf.addAttribute(white);

    redScarf = Cloth.builder()
        .name("빨간 스카프")
        .clothType(ClothType.SCARF)
        .user(user)
        .build();
    redScarf.addAttribute(red);

    sut.save(thinScarf);
    Thread.sleep(1);
    sut.save(whiteScarf);
    Thread.sleep(1);
    sut.save(redScarf);
  }

  @Test
  @DisplayName("저장 성공")
  void save_success() {
    // given
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

  @Nested
  @DisplayName("findByIdWithAttributes")
  class FindCltohAttributeByID {

    @Test
    @DisplayName("Cloth와 그 연관 Attribute들이 정상 조회되는지 확인")
    void findByIdWithAttributes_success() {
      // when
      Optional<Cloth> optionalCloth = sut.findByIdWithAttributes(thinScarf.getId());

      // then
      assertThat(optionalCloth).isPresent();
      Cloth cloth = optionalCloth.get();

      assertThat(cloth.getName()).isEqualTo("얇은 스카프");
      assertThat(cloth.getClothesWithAttributes()).hasSize(2);

      List<String> attributeNames = cloth.getClothesWithAttributes().stream()
          .map(cwa -> cwa.getAttribute().getName())
          .toList();

      assertThat(attributeNames).containsExactlyInAnyOrder("색상", "두께");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 findByIdWithAttributes실패")
    void findByIdWithAttributes_fail() {
      // given
      UUID notExistId = UUID.randomUUID();

      // when
      Optional<Cloth> result = sut.findByIdWithAttributes(notExistId);

      // then
      assertThat(result).isNotPresent();
    }
  }

  @Nested
  @DisplayName("findAllByIdWithAttributes(List<UUID> clothIds)")
  class FindAllByClothIds {

    @Test
    @DisplayName("여러 Cloth ID로 조회 시 각 Cloth와 연관 Attribute들이 함께 로딩")
    void findAllByIdWithAttributes_success() {
      // given
      List<UUID> clothIds = List.of(thinScarf.getId(), whiteScarf.getId(), redScarf.getId());

      // when
      List<Cloth> results = sut.findAllByIdWithAttributes(clothIds);
      // then
      assertThat(results).hasSize(3);

      results.forEach(cloth -> {
        assertThat(cloth.getClothesWithAttributes()).isNotEmpty();
        cloth.getClothesWithAttributes().forEach(cwa -> {
          assertThat(cwa.getAttribute()).isNotNull();
          assertThat(cwa.getAttribute().getName()).isNotBlank();
        });
      });
    }

    @Test
    @DisplayName("모든 ID가 존재하지 않는 경우 - 빈 리스트 반환")
    void findAllByIdWithAttributes_allNonExistent() {
      // given
      List<UUID> clothIds = List.of(UUID.randomUUID(), UUID.randomUUID());

      // when
      List<Cloth> results = sut.findAllByIdWithAttributes(clothIds);

      // then
      assertThat(results).isEmpty();
    }
  }

  @Nested
  @DisplayName("findAllWithAttributesByUserId")
  class FindAllWithAttributesByUserId {

    @Test
    @DisplayName("유저 ID로 해당 유저의 모든 옷과 속성이 정상 조회되는지 확인")
    void findAllWithAttributesByUserId_success() {
      // when
      List<Cloth> clothes = sut.findAllWithAttributesByUserId(user.getId());

      // then
      assertThat(clothes).hasSize(3);

      // 각 옷의 연관 속성이 즉시 로딩되었는지 확인
      for (Cloth cloth : clothes) {
        assertThat(cloth.getClothesWithAttributes()).isNotEmpty();
        for (ClothWithAttributes attr : cloth.getClothesWithAttributes()) {
          assertThat(attr.getAttribute()).isNotNull();
          assertThat(attr.getAttribute().getName()).isNotBlank();
        }
      }
    }

    @Test
    @DisplayName("존재하지 않는 유저 ID로 조회 시 빈 리스트 반환")
    void findAllWithAttributesByUserId_fail() {
      // given
      UUID invalidUserId = UUID.randomUUID();

      // when
      List<Cloth> result = sut.findAllWithAttributesByUserId(invalidUserId);

      // then
      assertThat(result).isEmpty();
    }

  }

  @Nested
  @DisplayName("search")
  class searchCloth {

    @Test
    @DisplayName("옷 조회 성공 - hasNext = false")
    void search_success() {
      //given
      int limit = 20;

      //when
      Slice<Cloth> clothList = sut.searchCloths(null, null, limit, ClothType.SCARF, user.getId());
      List<Cloth> content = clothList.getContent();
      //then
      assertThat(clothList.hasNext()).isFalse();
      assertThat(content).hasSize(3);
      assertThat(content.get(0)).isEqualTo(redScarf);
      assertThat(content.get(1)).isEqualTo(whiteScarf);
      assertThat(content.get(2)).isEqualTo(thinScarf);
    }

    @Test
    @DisplayName("옷 조회 성공 - hasNext=true")
    void search_success_hasNext() {
      //given
      int limit = 2;

      //when
      Slice<Cloth> clothList = sut.searchCloths(null, null, limit, ClothType.SCARF, user.getId());
      List<Cloth> content = clothList.getContent();

      //then
      assertThat(clothList.hasNext()).isTrue();
      assertThat(content).hasSize(2);
      assertThat(content.get(0)).isEqualTo(redScarf);
      assertThat(content.get(1)).isEqualTo(whiteScarf);
    }

    @Test
    @DisplayName("속성 조회 성공 - 커서 존재")
    void search_success_cursor() {
      //given
      int limit = 2;
      Instant cursor = thinScarf.getCreatedAt().truncatedTo(ChronoUnit.MICROS);

      //when
      Slice<Cloth> clothList = sut.searchCloths(cursor, null, limit, ClothType.SCARF, user.getId());
      List<Cloth> content = clothList.getContent();

      //then
      assertThat(clothList.hasNext()).isTrue();
      assertThat(content).hasSize(2);
      assertThat(content.get(0)).isEqualTo(redScarf);
      assertThat(content.get(1)).isEqualTo(whiteScarf);
    }
  }

}
