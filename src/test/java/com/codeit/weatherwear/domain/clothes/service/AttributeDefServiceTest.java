package com.codeit.weatherwear.domain.clothes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.codeit.weatherwear.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.codeit.weatherwear.domain.clothes.dto.response.ClothesAttributeDefDto;
import com.codeit.weatherwear.domain.clothes.dto.request.ClothesAttributeDefUpdateRequest;

import com.codeit.weatherwear.domain.clothes.entity.Attribute;
import com.codeit.weatherwear.domain.clothes.exception.AttributeAlreadyExistsException;
import com.codeit.weatherwear.domain.clothes.exception.AttributeNotFoundException;
import com.codeit.weatherwear.domain.clothes.exception.SelectableDuplicateException;
import com.codeit.weatherwear.domain.clothes.mapper.AttributeMapper;
import com.codeit.weatherwear.domain.clothes.repository.AttributeRepository;
import com.codeit.weatherwear.domain.clothes.repository.ClothWithAttributesRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AttributeDefServiceTest {

  @Mock
  private AttributeRepository attributeRepository;

  @Mock
  private AttributeMapper attributeMapper;

  @Mock
  private ClothWithAttributesRepository clothWithAttributesRepository;

  @InjectMocks
  private AttributeServiceImpl sut;

  private UUID attributeId;
  private Instant now;
  private Attribute sampleAttribute;

  @BeforeEach
  void setUp() {
    attributeId = UUID.randomUUID();
    now = Instant.now();
    sampleAttribute = Attribute.builder()
        .id(attributeId)
        .createdAt(now)
        .updatedAt(now)
        .name("색상")
        .selectableValues(new ArrayList<>(List.of("빨강", "파랑")))
        .build();
  }


  @Nested
  @DisplayName("속성 등록 테스트")
  class RegisterAttributeDef {

    @Test
    void createAttributes_Success() {
      //given
      ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest(
          "색상",
          List.of("빨강", "파랑"));
      Attribute attributes = Attribute.builder()
          .id(UUID.randomUUID())
          .createdAt(Instant.now())
          .updatedAt(Instant.now())
          .name("색상")
          .selectableValues(List.of("빨강", "파랑")).build();
      ClothesAttributeDefDto dto = new ClothesAttributeDefDto(attributes.getId(),
          attributes.getName(), attributes.getSelectableValues());
      given(attributeRepository.save(any(Attribute.class))).willReturn(attributes);
      given(attributeMapper.toDto(any(Attribute.class))).willReturn(dto);
      //when
      ClothesAttributeDefDto result = sut.create(request);
      //then
      assertThat(result.name()).isEqualTo("색상");
      assertThat(result.selectableValues()).containsExactly("빨강", "파랑");
      verify(attributeRepository, times(1)).save(any(Attribute.class));
    }

    @Test
    @DisplayName("속성 등록 실패 - 중복 속성 입력")
    void createAttributes_Fail() {
      //given
      given(attributeRepository.existsByName("사이즈")).willReturn(true);
      ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest(
          "사이즈",
          List.of("L")
      );
      //when
      //then
      assertThatThrownBy(() -> sut.create(request))
          .isInstanceOf(AttributeAlreadyExistsException.class)
          .hasMessage("속성 등록 실패");
    }
  }

  @Nested
  @DisplayName("속성 수정 테스트")
  class UpdateAttributeDef {

    @Test
    @DisplayName("수정 성공")
    void updateAttributes_Success() {
      //given
      given(attributeRepository.findById(attributeId)).willReturn(Optional.of(sampleAttribute));

      ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest("색상",
          List.of("노랑"));
      given(attributeMapper.toDto(sampleAttribute))
          .willReturn(new ClothesAttributeDefDto(attributeId, "색상", List.of("빨강", "파랑", "노랑")));
      //when
      ClothesAttributeDefDto result = sut.update(attributeId, request);
      //then
      assertThat(result.name()).isEqualTo("색상");
      assertThat(result.selectableValues()).containsExactly("빨강", "파랑", "노랑");
      verify(attributeRepository, times(1)).findById(attributeId);
    }

    @Test
    @DisplayName("수정 실패 - 존재하지 않는 속성일 경우")
    void updateAttributes_NotFound() {
      //given
      UUID id = UUID.randomUUID();
      given(attributeRepository.findById(id)).willReturn(Optional.empty());

      ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest("색상",
          List.of("노랑"));
      //when
      //then
      assertThatThrownBy(() -> sut.update(id, request))
          .isInstanceOf(AttributeNotFoundException.class)
          .hasMessage("속성 확인 실패");
      verify(attributeRepository).findById(id);
    }

    @Test
    @DisplayName("수정 실패 - 옷에서 사용 중일 경우")
    void updateAttributes_Used_Attribute() {
      //given
      given(attributeRepository.findById(attributeId)).willReturn(Optional.of(sampleAttribute));

      ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest("색상",
          List.of("노랑"));

      given(clothWithAttributesRepository.findUsedValuesByAttribute(attributeId))
          .willReturn(List.of("빨강"));
      //when
      //then
      assertThatThrownBy(() -> sut.update(attributeId, request))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("이미 사용 중인 속성은 수정할 수 없습니다.");
      verify(attributeRepository).findById(attributeId);
      verify(clothWithAttributesRepository).findUsedValuesByAttribute(attributeId);
    }

    @Test
    @DisplayName("수정 실패 - 기존에 등록된 selectableValues 요청 경우")
    void updateAttributes_Already_Exists_SelectableValues() {
      //given
      given(attributeRepository.findById(attributeId)).willReturn(Optional.of(sampleAttribute));
      ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest("색상",
          List.of("빨강"));
      // when & then
      assertThatThrownBy(() -> sut.update(attributeId, request))
          .isInstanceOf(SelectableDuplicateException.class)
          .hasMessage("속성 값 중복 등록");
      verify(attributeRepository).findById(attributeId);
    }

    @Test
    @DisplayName("수정 실패 - 중복된 selectableValues 요청 경우")
    void updateAttributes_Duplicate_SelectableValues() {
      //given
      given(attributeRepository.findById(attributeId)).willReturn(Optional.of(sampleAttribute));
      ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest("색상",
          List.of("노랑", "노랑"));
      // when & then
      assertThatThrownBy(() -> sut.update(attributeId, request))
          .isInstanceOf(SelectableDuplicateException.class)
          .hasMessage("속성 값 중복 등록");
      verify(attributeRepository).findById(attributeId);
    }
  }

  @Nested
  @DisplayName("속성 삭제 테스트")
  class DeleteAttributeDef {

    @Test
    @DisplayName("삭제 성공")
    void deleteAttributes_Success() {
      //given
      UUID id = UUID.randomUUID();
      Attribute attributes = Attribute.builder()
          .id(id)
          .createdAt(Instant.now())
          .updatedAt(Instant.now())
          .name("색상")
          .selectableValues(new ArrayList<>(List.of("빨강", "파랑"))).build();
      given(attributeRepository.findById(id)).willReturn(Optional.of(attributes));
      //when
      sut.delete(id);
      //then
      verify(attributeRepository, times(1)).findById(id);
      verify(attributeRepository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("삭제 실패 - 존재하지 않는 속성일 경우")
    void deleteAttribute_NotFound() {
      // given
      UUID id = UUID.randomUUID();
      given(attributeRepository.findById(id)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> sut.delete(id))
          .isInstanceOf(AttributeNotFoundException.class)
          .hasMessage("속성 확인 실패");

      verify(attributeRepository).findById(id);
      verify(attributeRepository, never()).deleteById(any());
    }

  }
}
