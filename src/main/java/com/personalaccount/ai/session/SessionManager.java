package com.personalaccount.ai.session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionManager {

    private static final String SESSION_PREFIX = "ai:conversation:";
    private static final Duration SESSION_TTL = Duration.ofMinutes(30);

    private final RedisTemplate<String, ConversationSession> redisTemplate;

    /**
     * 새 세션 생성
     */
    public ConversationSession createSession(Long userId, Long bookId) {
        String conversationId = generateConversationId();

        ConversationSession session = ConversationSession.builder()
                .conversationId(conversationId)
                .userId(userId)
                .bookId(bookId)
                .createdAt(LocalDateTime.now())
                .lastAccessedAt(LocalDateTime.now())
                .build();

        saveSession(session);

        log.debug("새 세션 생성: conversationId={}", conversationId);

        return session;
    }

    /**
     * 세션 조회
     */
    public ConversationSession getSession(String conversationId) {
        String key = SESSION_PREFIX + conversationId;
        ConversationSession session = redisTemplate.opsForValue().get(key);

        if (session != null) {
            // TTL 갱신
            redisTemplate.expire(key, SESSION_TTL);
        }

        return session;
    }

    /**
     * 세션 저장 (TTL 30분)
     */
    public void saveSession(ConversationSession session) {
        String key = SESSION_PREFIX + session.getConversationId();
        redisTemplate.opsForValue().set(key, session, SESSION_TTL);
    }

    /**
     * 세션 삭제
     */
    public void deleteSession(String conversationId) {
        String key = SESSION_PREFIX + conversationId;
        redisTemplate.delete(key);
        log.debug("세션 삭제: conversationId={}", conversationId);
    }

    /**
     * conversationId 생성
     */
    private String generateConversationId() {
        return "conv-" + UUID.randomUUID().toString().substring(0, 8);
    }
}