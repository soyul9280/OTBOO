package com.codeit.weatherwear.domain.clothes.service;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


import java.util.List;
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
    private ClothesRepository repository;
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
            UUID id=UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            UUID colorId= UUID.randomUUID();
            UUID sizeId= UUID.randomUUID();
            ClothesAttributeDto color = new ClothesAttributeDto(colorId, "파랑");
            ClothesAttributeDto size = new ClothesAttributeDto(sizeId, "S");
            ClothesCreateRequest request = new ClothesCreateRequest(
                ownerId,
                "후드티",
                ClothesType.TOP,
                List.of(color,size)
            );
            ClothesAttributeWithDefDto colorDto = new ClothesAttributeWithDefDto(colorId,
                "색상", List.of("빨강", "파랑"), "파랑");
            ClothesAttributeWithDefDto sizeDto = new ClothesAttributeWithDefDto(sizeId,
                "사이즈", List.of("S", "L"), "S");

            ClothesDto clothesDto = new ClothesDto(id, ownerId, "후드티", null, ClothesType.TOP,
                List.of(colorDto,sizeDto));

            given(repository.save(any(Clothes.class))).willReturn(clothes);
            given(mapper.toDto(any(Clothes.class))).willReturn(clothesDto);
            //when
            ClothesDto result = sut.create(request);
            //then
            assertThat(result.name()).isEqualTo("후드티");
            assertThat(result.selectDto().value).isEqualTo("파랑");
            verify(repository, times(1)).save(any(Clothes.class));
        }
    }
}
