package com.codeit.weatherwear.domain.clothes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.codeit.weatherwear.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.codeit.weatherwear.domain.clothes.dto.response.ClothesAttributeDefDto;
import com.codeit.weatherwear.domain.clothes.dto.request.ClothesAttributeDefUpdateRequest;

import com.codeit.weatherwear.domain.clothes.entity.Attribute;
import com.codeit.weatherwear.domain.clothes.exception.AttributeAlreadyExistsException;
import com.codeit.weatherwear.domain.clothes.exception.InvalidAttributeNameException;
import com.codeit.weatherwear.domain.clothes.mapper.AttributeMapper;
import com.codeit.weatherwear.domain.clothes.repository.AttributeRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

    @InjectMocks
    private AttributeServiceImpl sut;

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
            UUID id = UUID.randomUUID();
            Attribute attributes = Attribute.builder()
                .id(id)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .name("색상")
                .selectableValues(new ArrayList<>(List.of("빨강"))).build();
            given(attributeRepository.findById(id)).willReturn(Optional.of(attributes));

            ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest("색상",
                List.of("노랑"));
            given(attributeMapper.toDto(attributes))
                .willReturn(new ClothesAttributeDefDto(id, "색상", List.of("빨강", "노랑")));
            //when
            ClothesAttributeDefDto result = sut.update(id, request);
            //then
            assertThat(result.name()).isEqualTo("색상");
            assertThat(result.selectableValues()).containsExactly("빨강", "노랑");
            verify(attributeRepository, times(1)).findById(id);
        }

        @Test
        @DisplayName("수정 실패- 이름 불일치")
        void updateAttributes_Fail() {
            //given
            UUID id = UUID.randomUUID();
            Attribute attributes = Attribute.builder()
                .id(id)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .name("색상")
                .selectableValues(new ArrayList<>(List.of("빨강", "파랑"))).build();
            given(attributeRepository.findById(id)).willReturn(Optional.of(attributes));

            ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest("사이즈",
                List.of("S", "L"));
            //when
            //then
            assertThatThrownBy(() -> sut.update(id, request))
                .isInstanceOf(InvalidAttributeNameException.class)
                .hasMessage("잘못된 속성명입니다.");
            verify(attributeRepository, times(1)).findById(id);
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
    }

    @Nested
    @DisplayName("속성 조회 테스트")
    class FindAttributeDef {

        @Test
        @DisplayName("조회 성공")
        void findAttributes_Success() {

        }
    }
}
