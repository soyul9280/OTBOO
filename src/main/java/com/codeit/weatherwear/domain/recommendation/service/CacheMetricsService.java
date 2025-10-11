package com.codeit.weatherwear.domain.recommendation.service;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheMetricsService {

  private final CacheManager cacheManager;

  public void logRecommendationCacheStats() {
    CaffeineCache cache = (CaffeineCache) cacheManager.getCache("recommendations");
    if (cache != null) {
      CacheStats stats = cache.getNativeCache().stats();
      log.info("[Cache Stats] Hits: {}, Misses:{}, Hit Rate:{}"
          , stats.hitCount(), stats.missCount(), stats.hitRate());
    }
  }

}
