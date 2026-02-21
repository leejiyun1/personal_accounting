package com.personalaccount.common.ratelimit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;

    private static final int MAX_ATTEMPTS = 5;  // 최대 시도 횟수
    private static final long WINDOW_SIZE_SECONDS = 60;  // 1분 윈도우

    @Getter
    @RequiredArgsConstructor
    public enum KeyType {
        LOGIN("login");

        private final String prefix;
    }

    /**
     * Sliding Window 기반 Rate Limit 검증
     *
     * @param keyType 키 타입
     * @param identifier 식별자
     * @return true: 허용, false: 제한
     */
    public boolean tryConsume(KeyType keyType, String identifier) {
        String key = buildKey(keyType, identifier);
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - (WINDOW_SIZE_SECONDS * 1000);

        try {
            // 1. 만료된 요청 삭제 (Sliding Window)
            redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);

            // 2. 현재 윈도우 내 요청 개수 확인
            Long count = redisTemplate.opsForZSet().zCard(key);

            if (count != null && count >= MAX_ATTEMPTS) {
                log.warn("Rate limit 초과: key={}, count={}", key, count);
                return false;
            }

            // 3. 현재 요청 기록
            redisTemplate.opsForZSet().add(key, String.valueOf(currentTime), currentTime);

            // 4. TTL 설정 (메모리 관리)
            redisTemplate.expire(key, Duration.ofSeconds(WINDOW_SIZE_SECONDS));

            log.debug("Rate limit 통과: key={}, count={}", key, (count != null ? count + 1 : 1));
            return true;

        } catch (Exception e) {
            log.error("Rate limit 처리 실패: key={}", key, e);
            // Redis 장애 시 차단 (Fail-closed)
            return false;
        }
    }

    /**
     * Rate Limit 초기화 (로그인 성공 시)
     */
    public void reset(KeyType keyType, String identifier) {
        String key = buildKey(keyType, identifier);
        redisTemplate.delete(key);
        log.debug("Rate limit 초기화: key={}", key);
    }

    /**
     * 남은 시도 횟수 조회
     */
    public int getRemainingAttempts(KeyType keyType, String identifier) {
        String key = buildKey(keyType, identifier);
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - (WINDOW_SIZE_SECONDS * 1000);

        try {
            redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);
            Long count = redisTemplate.opsForZSet().zCard(key);
            return MAX_ATTEMPTS - (count != null ? count.intValue() : 0);
        } catch (Exception e) {
            log.error("남은 시도 횟수 조회 실패: key={}", key, e);
            return MAX_ATTEMPTS;
        }
    }

    private String buildKey(KeyType keyType, String identifier) {
        String hashedIdentifier = hashIdentifier(identifier);
        return "rate_limit:" + keyType.getPrefix() + ":" + hashedIdentifier;
    }

    private String hashIdentifier(String identifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(identifier.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
