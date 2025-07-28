package com.codeit.weatherwear.domain.clothes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 속성 정의 수정용
 *
 * @param name
 * @param selectableValues
 */
public record ClothesAttributeDefUpdateRequest(
    @NotBlank(message = "속성명을 입력해주세요")String name,
    @NotEmpty(message = "선택값 입력은 필수입니다.") List<String> selectableValues
) {
}
