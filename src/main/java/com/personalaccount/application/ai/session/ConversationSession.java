package com.personalaccount.application.ai.session;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationSession implements Serializable {

    private String conversationId;

    private Long userId;

    private Long bookId;

    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    private LocalDateTime createdAt;

    private LocalDateTime lastAccessedAt;

    // === 비즈니스 메서드 ===

    public void addMessage(String role, String content) {
        this.messages.add(new ChatMessage(role, content));
        this.lastAccessedAt = LocalDateTime.now();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage implements Serializable {
        private String role;     // "user", "assistant"
        private String content;  // 메시지 내용
    }
}