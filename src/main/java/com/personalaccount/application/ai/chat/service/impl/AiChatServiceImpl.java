package com.personalaccount.application.ai.chat.service.impl;

import com.personalaccount.application.ai.chat.dto.request.AiChatRequest;
import com.personalaccount.application.ai.chat.dto.request.GeminiRequest;
import com.personalaccount.application.ai.chat.dto.response.AiChatResponse;
import com.personalaccount.application.ai.chat.dto.response.GeminiResponse;
import com.personalaccount.application.ai.chat.service.AiChatService;
import com.personalaccount.application.ai.chat.service.PromptCacheService;
import com.personalaccount.application.ai.chat.service.TransactionCreationService;
import com.personalaccount.application.ai.session.ConversationSession;
import com.personalaccount.common.exception.custom.AiServiceException;
import com.personalaccount.common.exception.custom.BookNotFoundException;
import com.personalaccount.common.exception.custom.SessionNotFoundException;
import com.personalaccount.common.exception.custom.UnauthorizedBookAccessException;
import com.personalaccount.domain.ai.client.AiClient;
import com.personalaccount.domain.ai.repository.SessionRepository;
import com.personalaccount.domain.book.entity.Book;
import com.personalaccount.domain.book.entity.BookType;
import com.personalaccount.domain.book.repository.BookRepository;
import com.personalaccount.domain.transaction.dto.response.TransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final AiClient aiClient;
    private final SessionRepository sessionRepository;
    private final BookRepository bookRepository;
    private final TransactionCreationService transactionCreationService;
    private final PromptCacheService promptCacheService;

    @Override
    public AiChatResponse chat(Long userId, AiChatRequest request) {
        log.info("AI 대화 시작 - userId: {}, bookId: {}", userId, request.getBookId());

        // 1. 검증 (트랜잭션 없음)
        Book book = validateAndGetBook(userId, request.getBookId());
        BookType bookType = book.getBookType();

        // 2. 세션 처리 (Redis)
        ConversationSession session = getOrCreateSession(
                request.getConversationId(),
                userId,
                request.getBookId()
        );
        session.addMessage("user", request.getMessage());

        // 3. Gemini 요청 생성
        GeminiRequest geminiRequest = buildGeminiRequest(session, bookType);

        // 4. 외부 API 호출 (트랜잭션 밖)
        GeminiResponse geminiResponse = callGeminiApi(geminiRequest);
        String aiMessage = extractMessage(geminiResponse);

        session.addMessage("assistant", aiMessage);

        // 5. 응답 처리
        if (isTransactionComplete(aiMessage)) {
            return handleCompleteTransaction(userId, session, aiMessage, bookType);
        }

        return handleMoreInfoNeeded(session, aiMessage);
    }

    // === 검증 ===

    private Book validateAndGetBook(Long userId, Long bookId) {
        Book book = bookRepository.findByIdAndIsActive(bookId, true)
                .orElseThrow(() -> new BookNotFoundException(bookId));

        if (!book.getUser().getId().equals(userId)) {
            throw new UnauthorizedBookAccessException(bookId);
        }

        return book;
    }

    // === 세션 ===

    private ConversationSession getOrCreateSession(
            String conversationId,
            Long userId,
            Long bookId
    ) {
        if (conversationId == null || conversationId.isEmpty()) {
            return sessionRepository.createSession(userId, bookId);
        }

        ConversationSession session = sessionRepository.getSession(conversationId);
        if (session == null) {
            throw new SessionNotFoundException(conversationId);
        }

        return session;
    }

    // === Gemini API ===

    private GeminiRequest buildGeminiRequest(ConversationSession session, BookType bookType) {
        String cachedContentName = promptCacheService.getOrCreateCache(bookType);

        String conversation = buildConversationText(session);

        return GeminiRequest.builder()
                .cachedContent(cachedContentName)
                .contents(List.of(GeminiRequest.Content.builder()
                        .parts(List.of(GeminiRequest.Part.builder()
                                .text(conversation)
                                .build()))
                        .build()))
                .build();
    }

    private String buildConversationText(ConversationSession session) {
        StringBuilder sb = new StringBuilder();
        for (ConversationSession.ChatMessage message : session.getMessages()) {
            sb.append(message.getRole())
                    .append(": ")
                    .append(message.getContent())
                    .append("\n");
        }
        return sb.toString();
    }

    private GeminiResponse callGeminiApi(GeminiRequest request) {
        GeminiResponse response = aiClient.sendMessage(request).block();

        if (response == null) {
            throw new AiServiceException("AI 응답이 비어있습니다");
        }

        return response;
    }

    private String extractMessage(GeminiResponse response) {
        return response.getCandidates().getFirst()
                .getContent()
                .getParts().getFirst()
                .getText();
    }

    // === 응답 처리 ===

    private boolean isTransactionComplete(String message) {
        return message.startsWith("COMPLETE:");
    }

    private AiChatResponse handleCompleteTransaction(
            Long userId,
            ConversationSession session,
            String aiMessage,
            BookType bookType
    ) {
        log.info("거래 생성 - conversationId: {}", session.getConversationId());

        // 별도 서비스에서 트랜잭션 처리
        TransactionResponse transaction = transactionCreationService.createFromAiResponse(
                userId,
                session.getBookId(),
                bookType,
                aiMessage
        );

        // 세션 삭제
        sessionRepository.deleteSession(session.getConversationId());

        return AiChatResponse.builder()
                .conversationId(null)
                .needsMoreInfo(false)
                .message("거래가 생성되었습니다!")
                .transaction(transaction)
                .build();
    }

    private AiChatResponse handleMoreInfoNeeded(
            ConversationSession session,
            String aiMessage
    ) {
        sessionRepository.saveSession(session);

        return AiChatResponse.builder()
                .conversationId(session.getConversationId())
                .needsMoreInfo(true)
                .message(aiMessage)
                .build();
    }
}