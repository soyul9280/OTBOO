package com.codeit.weatherwear.domain.security;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtBlacklist {

  /**
   * key = access token / value = expiration time
   */
  private Map<String, Instant> blacklist = new ConcurrentHashMap();

  public void addBlacklist(String accessToken, Instant expirationTime) {
    blacklist.put(accessToken, expirationTime);
  }

  public boolean existsInBlacklist(String accessToken) {
    return blacklist.containsKey(accessToken);
  }

  // 메모리 누수 방지를 위해 1시간마다 만료된 액세스 토큰 삭제
  @Scheduled(cron = "0 0 0/1 * * *")
  public void cleanExpiredToken() {
    log.info("Clean JwtBlacklist: delete expired access tokens in list");
    Instant now = Instant.now();
    blacklist.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
  }
}