package com.codeit.weatherwear.domain.clothes.service;

import com.codeit.weatherwear.domain.clothes.dto.request.ClothesAttributeDto;
import com.codeit.weatherwear.domain.clothes.dto.request.ClothesCreateRequest;
import com.codeit.weatherwear.domain.clothes.dto.request.ClothesUpdateRequest;
import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import com.codeit.weatherwear.domain.clothes.entity.Attribute;
import com.codeit.weatherwear.domain.clothes.entity.Cloth;

import com.codeit.weatherwear.domain.clothes.entity.ClothWithAttributes;
import com.codeit.weatherwear.domain.clothes.mapper.ClothMapper;
import com.codeit.weatherwear.domain.clothes.repository.AttributeRepository;
import com.codeit.weatherwear.domain.clothes.repository.ClothRepository;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.exception.UserNotFoundException;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import java.util.List;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClothServiceImpl implements ClothService {

    private final ClothRepository clothRepository;
    private final AttributeRepository attributeRepository;
    private final UserRepository userRepository;
    private final ClothMapper clothMapper;

    /**
     * 의상 등록
     *
     * @param request 의상 생성 요청 DTO
     * @return 의상 DTO
     */
    @Override
    public ClothesDto create(ClothesCreateRequest request) {
        //사용자 찾기
        User user = userRepository.findById(request.ownerId())
            .orElseThrow(UserNotFoundException::new);

        List<UUID> attributesIds = request.attributes().stream()
            .map(ClothesAttributeDto::definitionId).toList();

        //속성 찾기
        List<Attribute> attributesList=attributeRepository.findAllById(attributesIds);

        Cloth cloth=Cloth.builder()
            .name(request.name())
            .clothType(request.type())
            .user(user)
            .build();

        //의상에 속성 적용
        Map<UUID, Attribute> attrMap = attributesList.stream()
            .collect(Collectors.toMap(Attribute::getId, Function.identity()));

        applyAttributesToCloth(request.attributes(), attrMap, cloth);

        Cloth saveCloth = clothRepository.save(cloth);

        return clothMapper.toDto(saveCloth);
    }

    /**
     * 의상 수정
     *
     * @param clothesId 수정 요청 ID
     * @param request 수정 요청 DTO
     * @return 의상 DTO
     */
    @Override
    public ClothesDto update(UUID clothesId,ClothesUpdateRequest request) {
        Cloth cloth = clothRepository.findById(clothesId)
            .orElseThrow(() -> new IllegalArgumentException("의상을 찾을 수 없습니다"));

        List<UUID> attrIds = request.attributes().stream()
            .map(ClothesAttributeDto::definitionId)
            .toList();
        List<Attribute> attributes = attributeRepository.findAllById(attrIds);

        cloth.clearAttributes();
        cloth.updateCloth(request.name(),request.type());

        Map<UUID, Attribute> attributeMap = attributes.stream()
            .collect(Collectors.toMap(Attribute::getId, Function.identity()));

        applyAttributesToCloth(request.attributes(), attributeMap, cloth);
        return clothMapper.toDto(cloth);
    }


    /**
     * 의상 삭제
     *
     * @param clothesId 의상 ID
     */
    @Override
    public void delete(UUID clothesId) {
        Cloth cloth = clothRepository.findById(clothesId)
            .orElseThrow(()->new IllegalArgumentException("의상을 찾을 수 없습니다"));
        clothRepository.delete(cloth);
    }


    private static void applyAttributesToCloth(List<ClothesAttributeDto> attributeDtos, Map<UUID, Attribute> attrMap,
        Cloth cloth) {
        for (ClothesAttributeDto dto : attributeDtos) {
            Attribute attribute = attrMap.get(dto.definitionId());
            if (attribute == null) {
                throw new IllegalArgumentException("존재하지 않는 속성입니다.");
            }
            if(!attribute.getSelectableValues().contains(dto.value())) {
                throw new IllegalArgumentException("선택한 속성 값이 존재하지 않습니다.");
            }

            ClothWithAttributes attr = ClothWithAttributes.builder()
                .value(dto.value())
                .attribute(attribute)
                .cloth(cloth)
                .build();

            cloth.addAttribute(attr);
        }
    }

}

