package com.codeit.weatherwear.domain.clothes.service;

import com.codeit.weatherwear.domain.clothes.dto.request.AttributesSearchRequest;
import com.codeit.weatherwear.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.codeit.weatherwear.domain.clothes.dto.request.ClothesAttributeDefUpdateRequest;
import com.codeit.weatherwear.domain.clothes.dto.response.ClothesAttributeDefDto;
import com.codeit.weatherwear.domain.clothes.entity.Attribute;
import com.codeit.weatherwear.domain.clothes.exception.AttributeAlreadyExistsException;
import com.codeit.weatherwear.domain.clothes.exception.AttributeNotFoundException;
import com.codeit.weatherwear.domain.clothes.exception.InvalidAttributeNameException;
import com.codeit.weatherwear.domain.clothes.exception.SelectableDuplicateException;
import com.codeit.weatherwear.domain.clothes.mapper.AttributeMapper;
import com.codeit.weatherwear.domain.clothes.repository.AttributeRepository;
import com.codeit.weatherwear.global.request.SortDirection;
import com.codeit.weatherwear.global.response.PageResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttributeServiceImpl implements AttributeService {

    private final AttributeRepository attributeRepository;
    private final AttributeMapper attributeMapper;

    /**
     * 속성 등록
     *
     * @param request 속성 생성 요청 DTO
     * @return 속성DTO
     */
    @Override
    @Transactional
    public ClothesAttributeDefDto create(ClothesAttributeDefCreateRequest request) {

        if (attributeRepository.existsByName(request.name())) {
            log.warn("[속성 정의 등록 실패] 이미 존재하는 속성명 : {}", request.name());
            throw new AttributeAlreadyExistsException();
        }

        Attribute attribute = Attribute.builder()
            .name(request.name())
            .selectableValues(request.selectValues())
            .build();

        Attribute save = attributeRepository.save(attribute);
        log.info("[속성 정의 등록 완료] id: {}, 속성명: {}", attribute.getId(), attribute.getName());

        return attributeMapper.toDto(save);
    }

    /**
     * 속성 수정
     *
     * @param id
     * @param request 속성 수정 요청 DTO
     * @return 속성 DTO
     */
    @Override
    @Transactional
    public ClothesAttributeDefDto update(UUID id, ClothesAttributeDefUpdateRequest request) {
        Attribute attribute = attributeRepository.findById(id)
            .orElseThrow(()->{
                log.warn("[속성 정의 수정 실패] 존재하지 않는 속성입니다. ID : {}", id);
                return new AttributeNotFoundException();
            });
        if(!attribute.getName().equals(request.name())) {
            log.warn("[속성 정의 수정 실패] 존재하지 않는 속성명이거나 속성명 요청이 잘못되었습니다. ID : {}", id);
            throw new InvalidAttributeNameException();
        }
        if(attribute.getSelectableValues().contains(request.selectValues())) {
            log.warn("[속성 정의 수정 실패] 중복된 속성 값을 입력하였습니다. ID : {}", id);
            throw new SelectableDuplicateException();
        }

        log.debug("[속성 정의 수정] name: {}, 변경 전 values: {}", attribute.getName(), attribute.getSelectableValues());
        attribute.update(request.name(), request.selectValues());

        log.info("[속성 정의 수정 완료] ID : {}, name: {}, values: {}", id, attribute.getName(), attribute.getSelectableValues());
        return attributeMapper.toDto(attribute);
    }

    /**
     * 속성 삭제
     *
     * @param id
     */
    @Override
    @Transactional
    public void delete(UUID id) {
        Attribute attribute = attributeRepository.findById(id).orElseThrow(()->{
                log.warn("[속성 정의 삭제 실패] 존재하지 않는 속성 ID: {}", id);
                throw new AttributeNotFoundException();
        });
        attributeRepository.deleteById(attribute.getId());
        log.info("[속성 정의 삭제 완료] ID: {}", id);
    }

    /**
     * 속성 조회
     * @param request 조회 조건
     * @return ClothesAttributeDefDto 결과 리스트
     */
    @Override
    public PageResponse<ClothesAttributeDefDto> searchAttributes(AttributesSearchRequest request) {
        String cursor = request.cursor();
        UUID idAfter = request.idAfter();
        int limit = request.limit();
        String sortBy = request.sortBy();
        SortDirection sortDirection = request.sortDirection();
        String keywordLike = request.keywordLike();

        Slice<Attribute> attributes = attributeRepository.searchAttributes(cursor, idAfter, limit,
            sortBy, sortDirection, keywordLike);
        List<Attribute> attributesList = attributes.getContent();

        log.debug("[쿼리 실행 결과] 전체 개수: {}, hasNext: {}", attributesList.size(), attributes.hasNext());

        List<ClothesAttributeDefDto> result = attributesList.stream()
            .map(attributeMapper::toDto)
            .toList();
        log.debug("[응답 변환] 변환된 ClothesAttributeDefDto 개수: {}", result.size());

        Attribute last =
            (attributesList.size() > 0) ? attributesList.get(attributesList.size() - 1) : null;
        Object nextCursor = null;
        UUID nextIdAfter = null;
        if (last != null) {
            switch (sortBy) {
                case "name":
                    nextCursor = last.getName();
                    break;
                case "createdAt":
                    nextCursor = last.getCreatedAt();
                    break;
                default:
                    throw new IllegalArgumentException("지원하지 않는 정렬 기준입니다.");
            }
            nextIdAfter = last.getId();
        }
        return new PageResponse<>(
            result,
            nextCursor,
            nextIdAfter,
            attributes.hasNext(),
            attributeRepository.getTotalCount(keywordLike),
            sortBy,
            sortDirection.name()
        );


    }


}
