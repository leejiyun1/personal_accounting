package com.personalaccount.domain.ai.repository;

import com.personalaccount.application.ai.session.ConversationSession;

public interface SessionRepository {

    /**
     * 새 세션 생성
     */
    ConversationSession createSession(Long userId, Long bookId);

    /**
     * 세션 조회
     */
    ConversationSession getSession(String conversationId);

    /**
     * 세션 저장
     */
    void saveSession(ConversationSession session);

    /**
     * 세션 삭제
     */
    void deleteSession(String conversationId);
}