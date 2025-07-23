package com.codeit.weatherwear.domain.clothes.service;

import com.codeit.weatherwear.domain.clothes.dto.request.AttributesSearchRequest;
import com.codeit.weatherwear.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.codeit.weatherwear.domain.clothes.dto.request.ClothesAttributeDefUpdateRequest;
import com.codeit.weatherwear.domain.clothes.dto.response.ClothesAttributeDefDto;
import com.codeit.weatherwear.domain.clothes.entity.Attribute;
import com.codeit.weatherwear.domain.clothes.exception.attribute.AttributeAlreadyExistsException;
import com.codeit.weatherwear.domain.clothes.exception.attribute.AttributeNotFoundException;
import com.codeit.weatherwear.domain.clothes.exception.attribute.SelectableDuplicateException;
import com.codeit.weatherwear.domain.clothes.mapper.AttributeMapper;
import com.codeit.weatherwear.domain.clothes.repository.AttributeRepository;
import com.codeit.weatherwear.domain.clothes.repository.ClothWithAttributesRepository;
import com.codeit.weatherwear.global.event.DomainEventPublisher;
import com.codeit.weatherwear.global.request.SortDirection;
import com.codeit.weatherwear.global.response.PageResponse;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
  private final ClothWithAttributesRepository clothWithAttributesRepository;
  private final DomainEventPublisher domainEventPublisher;


  /**
   * 속성 등록
   *
   * @param request 속성 생성 요청 DTO
   * @return 속성DTO
   */
  @Override
  @Transactional
  public ClothesAttributeDefDto create(ClothesAttributeDefCreateRequest request) {
    log.info("[Start Creating AttributeDef] AttributeDef Name: {}", request.name());
    if (attributeRepository.existsByName(request.name())) {
      log.warn("[Fail Creating AttributeDef] Already Exists AttributeDef : {}", request.name());
      throw new AttributeAlreadyExistsException();
    }

    Attribute attribute = Attribute.builder()
        .name(request.name())
        .selectableValues(request.selectableValues())
        .build();

    Attribute save = attributeRepository.save(attribute);
    log.info("[Creating AttributeDef Completed] Id: {}, AttributeDef Name: {}", attribute.getId(),
        attribute.getName());

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
    log.info("[Start Updating AttributeDef] ID: {}, AttributeDef Name: {}", id, request.name());
    Attribute attribute = attributeRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("[Fail Updating AttributeDef] AttributeDef Not Found, ID : {}", id);
          return new AttributeNotFoundException();
        });

    //입력값 중복일 때 에러발생 (ex) 여름 여름
    List<String> newValues = request.selectableValues();
    Set<String> uniqueValues = new LinkedHashSet<>(newValues);//순서 중요
    if (uniqueValues.size() != newValues.size()) {
      log.warn("[Fail Updating AttributeDef] Selectable Duplicate. ID : {}", id);
      throw new SelectableDuplicateException();
    }

    // 기존 값과 중복 검사
    List<String> existingValues = attribute.getSelectableValues();
    for (String newValue : newValues) {
      if (existingValues.contains(newValue)) {
        log.warn("[Fail Updating AttributeDef] Value already exists. ID : {}, value: {}", id,
            newValue);
        throw new SelectableDuplicateException();
      }
    }

    //옷에서 사용중인 속성은 수정 불가
    List<String> usedValues = clothWithAttributesRepository.findUsedValuesByAttribute(
        attribute.getId());
    for (String value : usedValues) {
      if (!newValues.contains(value)) {
        throw new IllegalStateException("Already Used AttributeDef By Cloth: " + value);
      }
    }

    log.debug("[Updating AttributeDef] Name: {}, Before Values: {}", attribute.getName(),
        attribute.getSelectableValues());
    attribute.update(request.name(), request.selectableValues());

    log.info("[Updating AttributeDef Completed] ID : {}, Name: {}, Values: {}", id,
        attribute.getName(),
        attribute.getSelectableValues());
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
    log.info("[Start Deleting AttributeDef] ID: {}", id);
    Attribute attribute = attributeRepository.findById(id).orElseThrow(() -> {
      log.warn("[Fail Deleting AttributeDef] AttributeDef Not Found, ID: {}", id);
      throw new AttributeNotFoundException();
    });
    attributeRepository.deleteById(attribute.getId());
    log.info("[Deleting AttributeDef Completed] ID: {}", id);
  }

  /**
   * 속성 조회
   *
   * @param request 조회 조건
   * @return ClothesAttributeDefDto 결과 리스트
   */
  @Override
  public PageResponse<ClothesAttributeDefDto> searchAttributes(AttributesSearchRequest request) {
    log.info("[Start Searching AttributeDef] keyword: {}, sortBy: {}, direction: {}, limit: {}",
        request.keywordLike(), request.sortBy(), request.sortDirection(), request.limit());

    String cursor = request.cursor();
    UUID idAfter = request.idAfter();
    int limit = request.limit();
    String sortBy = request.sortBy();
    SortDirection sortDirection = request.sortDirection();
    String keywordLike = request.keywordLike();

    Slice<Attribute> attributes = attributeRepository.searchAttributes(cursor, idAfter, limit,
        sortBy, sortDirection, keywordLike);
    List<Attribute> attributesList = attributes.getContent();

    log.debug("[Query Result] Total Count: {}, hasNext: {}", attributesList.size(),
        attributes.hasNext());

    List<ClothesAttributeDefDto> result = attributesList.stream()
        .map(attributeMapper::toDto)
        .toList();
    log.debug("[Response Result] Count Changed To ClothesAttributeDefDto: {}", result.size());

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
          throw new IllegalArgumentException("Unsupported cursor: " + request.cursor());
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
