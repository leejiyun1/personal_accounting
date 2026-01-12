package com.personalaccount.auth.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private static final String KEY_PREFIX = "refresh:";

    private final StringRedisTemplate redisTemplate;

    /**
     * Refresh Token 저장
     * - Key: refresh:{userId}
     * - Value: refreshToken
     */
    public void save(Long userId, String refreshToken, Duration ttl) {
        String key = KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(key, refreshToken, ttl);
        log.debug("Refresh Token 저장: userId={}", userId);
    }

    /**
     * Refresh Token 조회
     */
    public String findByUserId(Long userId) {
        String key = KEY_PREFIX + userId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Refresh Token 검증
     * - 저장된 토큰과 일치하는지 확인
     */
    public boolean validate(Long userId, String refreshToken) {
        String storedToken = findByUserId(userId);
        return refreshToken.equals(storedToken);
    }

    /**
     * Refresh Token 삭제
     */
    public void delete(Long userId) {
        String key = KEY_PREFIX + userId;
        redisTemplate.delete(key);
        log.debug("Refresh Token 삭제: userId={}", userId);
    }

    /**
     * Refresh Token 존재 여부
     */
    public boolean exists(Long userId) {
        String key = KEY_PREFIX + userId;
        Boolean exists = redisTemplate.hasKey(key);
        return exists != null && exists;
    }
}