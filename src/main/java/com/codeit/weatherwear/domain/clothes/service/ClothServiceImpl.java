package com.codeit.weatherwear.domain.clothes.service;

import com.codeit.weatherwear.domain.clothes.dto.request.ClothesAttributeDto;
import com.codeit.weatherwear.domain.clothes.dto.request.ClothesCreateRequest;
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
@Transactional(readOnly = true)
public class ClothServiceImpl implements ClothService {

    private final ClothRepository clothRepository;
    private final AttributeRepository attributeRepository;
    private final UserRepository userRepository;
    private final ClothMapper clothMapper;

    @Override
    @Transactional
    public ClothesDto create(ClothesCreateRequest request) {
        //사용자 찾기
        User user = userRepository.findById(request.ownerId())
            .orElseThrow(UserNotFoundException::new);

        List<UUID> attributesIds = request.attributes().stream()
            .map(ClothesAttributeDto::definitionId).toList();

        //속성 찾기
        List<Attribute> attributesList=attributeRepository.findAllById(attributesIds);
        Map<UUID,Attribute> attributesMap=attributesList.stream().collect(
            Collectors.toMap(Attribute::getId, Function.identity()));

        Cloth clothes=Cloth.builder()
            .name(request.name())
            .clothType(request.type())
            .user(user)
            .build();

        for (ClothesAttributeDto attrDto : request.attributes()) {
            Attribute attributes = attributesMap.get(attrDto.definitionId());
            if(attributes==null) {
                throw new IllegalArgumentException("존재하지 않는 속성입니다.");
            }
            ClothWithAttributes result = ClothWithAttributes.builder()
                .value(attrDto.value())
                .attribute(attributes)
                .build();
            clothes.addAttribute(result);
        }

        Cloth saveClothes = clothRepository.save(clothes);

        return clothMapper.toDto(saveClothes);
    }

    @Override
    @Transactional
    public void delete(UUID clothesId) {
        Cloth cloth = clothRepository.findById(clothesId)
            .orElseThrow(()->new IllegalArgumentException("존재하지 않는 옷입니다."));
        clothRepository.delete(cloth);
    }


}

