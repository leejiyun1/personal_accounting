package com.personalaccount.common.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    /**
     * Rate Limit 검증
     *
     * @param key 식별자 (IP, userId, email 등)
     * @return true: 허용, false: 제한
     */
    public boolean tryConsume(String key) {
        Bucket bucket = cache.computeIfAbsent(key, k -> createBucket());
        return bucket.tryConsume(1);
    }

    /**
     * Bucket 생성
     *
     * 정책: 1분에 5회 요청 허용
     */
    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(
                5,  // 5회 허용
                Refill.intervally(5, Duration.ofMinutes(1))  // 1분마다 5개 리필
        );
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * 특정 키의 제한 초기화 (로그인 성공 시)
     */
    public void reset(String key) {
        cache.remove(key);
        log.debug("Rate limit 초기화: key={}", key);
    }
}