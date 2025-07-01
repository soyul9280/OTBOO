package com.codeit.weatherwear.domain.ootd.service.impl;

import com.codeit.weatherwear.domain.clothes.entity.Cloth;
import com.codeit.weatherwear.domain.clothes.repository.ClothRepository;
import com.codeit.weatherwear.domain.feed.entity.Feed;
import com.codeit.weatherwear.domain.ootd.dto.response.OotdDto;
import com.codeit.weatherwear.domain.ootd.entity.Ootd;
import com.codeit.weatherwear.domain.ootd.mapper.OotdMapper;
import com.codeit.weatherwear.domain.ootd.repository.OotdRepository;
import com.codeit.weatherwear.domain.ootd.service.OotdService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OotdServiceImpl implements OotdService {

  private final OotdRepository ootdRepository;
  private final ClothRepository clothRepository;

  private final OotdMapper ootdMapper;

  @Transactional
  @Override
  public List<OotdDto> createOotdList(Feed feed, List<UUID> clothIds) {

    if (clothIds == null || clothIds.isEmpty()) {
      return List.of();
    }

    List<Cloth> clothesList = clothIds.stream()
        .map(clothId -> clothRepository.findById(clothId).orElseThrow()).toList();

    List<Ootd> ootdList = clothesList.stream()
        .map(cloth -> ootdMapper.toEntity(feed, cloth)).toList();

    List<Ootd> savedList = ootdRepository.saveAll(ootdList);

    return savedList.stream().map(ootdMapper::toDto).toList();
  }

  @Transactional
  @Override
  public List<OotdDto> findOotdByFeedId(UUID feedId) {

    List<Ootd> ootds = ootdRepository.findByFeedId(feedId);

    return ootds.stream().map(ootdMapper::toDto).toList();
  }

  @Transactional
  @Override
  public List<OotdDto> deleteOotdByFeedId(UUID feedId) {

    List<Ootd> ootds = ootdRepository.findByFeedId(feedId);
    ootdRepository.deleteAll(ootds);

    return ootds.stream().map(ootdMapper::toDto).toList();
  }
}
