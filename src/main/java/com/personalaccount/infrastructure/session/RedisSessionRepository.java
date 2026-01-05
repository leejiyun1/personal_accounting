package com.personalaccount.infrastructure.session;

import com.personalaccount.application.ai.session.ConversationSession;
import com.personalaccount.domain.ai.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisSessionRepository implements SessionRepository {

    private static final String SESSION_PREFIX = "ai:conversation:";
    private static final Duration SESSION_TTL = Duration.ofMinutes(30);

    private final RedisTemplate<String, ConversationSession> redisTemplate;

    @Override
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

    @Override
    public ConversationSession getSession(String conversationId) {
        String key = SESSION_PREFIX + conversationId;
        ConversationSession session = redisTemplate.opsForValue().get(key);

        if (session != null) {
            // TTL 갱신
            redisTemplate.expire(key, SESSION_TTL);
        }

        return session;
    }

    @Override
    public void saveSession(ConversationSession session) {
        String key = SESSION_PREFIX + session.getConversationId();
        redisTemplate.opsForValue().set(key, session, SESSION_TTL);
    }

    @Override
    public void deleteSession(String conversationId) {
        String key = SESSION_PREFIX + conversationId;
        redisTemplate.delete(key);
        log.debug("세션 삭제: conversationId={}", conversationId);
    }

    private String generateConversationId() {
        return "conv-" + UUID.randomUUID().toString().substring(0, 8);
    }
}