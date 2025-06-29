package com.codeit.weatherwear.domain.clothes.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


import com.codeit.weatherwear.domain.clothes.dto.request.ClothesAttributeDto;
import com.codeit.weatherwear.domain.clothes.dto.request.ClothesAttributeWithDefDto;
import com.codeit.weatherwear.domain.clothes.dto.request.ClothesCreateRequest;
import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import com.codeit.weatherwear.domain.clothes.entity.Attributes;
import com.codeit.weatherwear.domain.clothes.entity.Clothes;
import com.codeit.weatherwear.domain.clothes.entity.ClothesType;
import com.codeit.weatherwear.domain.clothes.mapper.ClothesMapper;
import com.codeit.weatherwear.domain.clothes.repository.AttributesRepository;
import com.codeit.weatherwear.domain.clothes.repository.ClothesRepository;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import java.time.Instant;
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
public class ClothesServiceTest {

    @Mock
    private AttributesRepository attributeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ClothesRepository clothesRepository;
    @Mock
    private ClothesMapper mapper;

    @InjectMocks
    private ClothesServiceImpl sut;

    @Nested
    @DisplayName("의상 등록 테스트")
    class RegisterClothes {

        @Test
        @DisplayName("등록 성공")
        void create_success() {
            //given
            UUID ownerId = UUID.randomUUID();
            UUID clothesId = UUID.randomUUID();
            UUID colorId = UUID.randomUUID();
            UUID sizeId = UUID.randomUUID();

            ClothesAttributeDto color = new ClothesAttributeDto(colorId, "파랑");
            ClothesAttributeDto size = new ClothesAttributeDto(sizeId, "S");
            ClothesCreateRequest request = new ClothesCreateRequest(
                ownerId,
                "후드티",
                ClothesType.TOP,
                List.of(color, size)
            );
            ClothesAttributeWithDefDto colorDto = new ClothesAttributeWithDefDto(colorId,
                "색상", List.of("빨강", "파랑"), "파랑");
            ClothesAttributeWithDefDto sizeDto = new ClothesAttributeWithDefDto(sizeId,
                "사이즈", List.of("S", "L"), "S");

            Attributes colorDef = Attributes.builder()
                .id(colorId)
                .name("색상")
                .selectableValues(List.of("빨강", "파랑"))
                .build();
            Attributes sizeDef = Attributes.builder()
                .id(sizeId)
                .name("사이즈")
                .selectableValues(List.of("S", "L"))
                .build();

            ClothesDto clothesDto = ClothesDto.builder()
                .id(clothesId)
                .ownerId(ownerId)
                .name("후드티")
                .imageUrl(null)
                .type(ClothesType.TOP)
                .attributes(List.of(colorDto, sizeDto))
                .build();

            Clothes clothes = Clothes.builder()
                .id(clothesId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .name("후드티")
                .clothesType(ClothesType.TOP)
                .clothesImageUrl(null)
                .user(User.builder().id(ownerId).build())
                .build();

            User mockUser = User.builder().id(ownerId).build();
            given(userRepository.findById(ownerId)).willReturn(Optional.of(mockUser));

            given(attributeRepository.findAllById(any())).willReturn(List.of(colorDef, sizeDef));
            given(clothesRepository.save(any(Clothes.class))).willReturn(clothes);
            given(mapper.toDto(any(Clothes.class))).willReturn(clothesDto);
            //when
            ClothesDto result = sut.create(request);
            //then
            assertThat(result.getName()).isEqualTo("후드티");
            assertThat(result.getAttributes().get(0).value()).isEqualTo("파랑");
            verify(clothesRepository, times(1)).save(any(Clothes.class));
        }

        @Test
        @DisplayName("의상 등록 실패 - 속성ID가 존재하지 않을 경우")
        void create_fail() {
            //given
            UUID ownerId = UUID.randomUUID();
            UUID attributesId = UUID.randomUUID();
            ClothesCreateRequest request = new ClothesCreateRequest(ownerId, "후드티",
                ClothesType.TOP,
                List.of(new ClothesAttributeDto(attributesId, "파랑"))
            );

            User mockUser = User.builder().id(ownerId).build();
            given(userRepository.findById(ownerId)).willReturn(Optional.of(mockUser));
            given(attributeRepository.findAllById(any())).willReturn(List.of()); // 속성 없음

            // when & then
            assertThatThrownBy(() -> sut.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 속성");

            verify(clothesRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("의상 삭제 테스트")
    class UpdateClothes {

        @Test
        @DisplayName("삭제 성공")
        void delete_success() {
            //given
            UUID ownerId = UUID.randomUUID();
            UUID clothesId = UUID.randomUUID();
            Clothes clothes = Clothes.builder()
                .id(clothesId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .name("후드티")
                .clothesType(ClothesType.TOP)
                .clothesImageUrl(null)
                .user(User.builder().id(ownerId).build())
                .build();

            given(clothesRepository.findById(clothesId)).willReturn(Optional.of(clothes));

            //when
            sut.delete(clothesId);
            //then
            verify(clothesRepository,times(1)).findById(clothesId);
            verify(clothesRepository,times(1)).delete(clothes);
        }
    }
}
