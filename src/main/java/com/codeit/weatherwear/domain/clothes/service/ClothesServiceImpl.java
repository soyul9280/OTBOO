package com.codeit.weatherwear.domain.clothes.service;

import com.codeit.weatherwear.domain.clothes.dto.request.ClothesAttributeDto;
import com.codeit.weatherwear.domain.clothes.dto.request.ClothesCreateRequest;
import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import com.codeit.weatherwear.domain.clothes.entity.Attributes;
import com.codeit.weatherwear.domain.clothes.entity.Clothes;
import com.codeit.weatherwear.domain.clothes.entity.ClothesWithAttributes;

import com.codeit.weatherwear.domain.clothes.mapper.ClothesMapper;
import com.codeit.weatherwear.domain.clothes.repository.AttributesRepository;
import com.codeit.weatherwear.domain.clothes.repository.ClothesRepository;
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
public class ClothesServiceImpl implements ClothesService {

    private final ClothesRepository clothesRepository;
    private final AttributesRepository attributeRepository;
    private final UserRepository userRepository;
    private final ClothesMapper clothesMapper;

    @Override
    @Transactional
    public ClothesDto create(ClothesCreateRequest request) {
        //사용자 찾기
        User user = userRepository.findById(request.ownerId())
            .orElseThrow(UserNotFoundException::new);

        List<UUID> attributesIds = request.attributes().stream()
            .map(ClothesAttributeDto::definitionId).toList();

        //속성 찾기
        List<Attributes> attributesList=attributeRepository.findAllById(attributesIds);
        Map<UUID,Attributes> attributesMap=attributesList.stream().collect(
            Collectors.toMap(Attributes::getId, Function.identity()));

        Clothes clothes=Clothes.builder()
            .name(request.name())
            .clothesType(request.type())
            .user(user)
            .build();

        for (ClothesAttributeDto attrDto : request.attributes()) {
            Attributes attributes = attributesMap.get(attrDto.definitionId());
            if(attributes==null) {
                throw new IllegalArgumentException("존재하지 않는 속성입니다.");
            }
            ClothesWithAttributes result = ClothesWithAttributes.builder()
                .value(attrDto.value())
                .attributes(attributes)
                .build();
            clothes.addAttributes(result);
        }

        Clothes saveClothes = clothesRepository.save(clothes);

        return clothesMapper.toDto(saveClothes);
    }

    @Override
    @Transactional
    public void delete(UUID clothesId) {
        Clothes clothes = clothesRepository.findById(clothesId)
            .orElseThrow(()->new IllegalArgumentException("존재하지 않는 옷입니다."));
        clothesRepository.delete(clothes);
    }


}

