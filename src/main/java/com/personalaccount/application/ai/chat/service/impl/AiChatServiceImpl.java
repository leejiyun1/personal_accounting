package com.personalaccount.application.ai.chat.service.impl;

import com.personalaccount.application.ai.dto.request.AiChatRequest;
import com.personalaccount.application.ai.dto.response.AiChatResponse;
import com.personalaccount.application.ai.chat.service.AiChatService;
import com.personalaccount.application.ai.chat.service.PromptCacheService;
import com.personalaccount.application.ai.chat.service.TransactionCreationService;
import com.personalaccount.application.ai.session.ConversationSession;
import com.personalaccount.common.exception.custom.AiServiceException;
import com.personalaccount.common.exception.custom.BookNotFoundException;
import com.personalaccount.common.exception.custom.SessionNotFoundException;
import com.personalaccount.common.exception.custom.UnauthorizedBookAccessException;
import com.personalaccount.domain.ai.client.AiClient;
import com.personalaccount.domain.ai.dto.AiMessageRequest;
import com.personalaccount.domain.ai.dto.AiMessageResponse;
import com.personalaccount.domain.ai.repository.SessionRepository;
import com.personalaccount.domain.book.entity.Book;
import com.personalaccount.domain.book.entity.BookType;
import com.personalaccount.domain.book.repository.BookRepository;
import com.personalaccount.domain.transaction.dto.response.TransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
    public Mono<AiChatResponse> chat(Long userId, AiChatRequest request) {
        log.info("AI 대화 시작 - userId: {}, bookId: {}", userId, request.getBookId());

        // 1. DB 조회 (블로킹) - boundedElastic 스케줄러에서 실행
        return Mono.fromCallable(() -> validateAndGetBook(userId, request.getBookId()))
                .subscribeOn(Schedulers.boundedElastic())

                // 2. 세션 처리 (Redis 블로킹) - 같은 boundedElastic에서 실행
                .map(book -> {
                    ConversationSession session = getOrCreateSession(
                            request.getConversationId(),
                            userId,
                            request.getBookId()
                    );
                    session.addMessage("user", request.getMessage());
                    return new ChatContext(book, session);
                })

                // 3. AI 요청 생성 및 호출 (논블로킹)
                .flatMap(context -> {
                    AiMessageRequest aiRequest = buildAiRequest(context.session(), context.book().getBookType());
                    
                    return aiClient.sendMessage(aiRequest)
                            .map(aiResponse -> {
                                if (aiResponse == null) {
                                    throw new AiServiceException("AI 응답이 비어있습니다");
                                }
                                return new AiResponseContext(context, aiResponse.getMessage());
                            });
                })

                // 4. 응답 처리 (DB 저장은 블로킹) - boundedElastic에서 실행
                .flatMap(responseContext -> 
                    Mono.fromCallable(() -> processResponse(userId, responseContext))
                            .subscribeOn(Schedulers.boundedElastic())
                );
    }

    // === 내부 컨텍스트 클래스 ===

    private record ChatContext(Book book, ConversationSession session) {}
    
    private record AiResponseContext(ChatContext chatContext, String aiMessage) {
        Book book() { return chatContext.book(); }
        ConversationSession session() { return chatContext.session(); }
    }

    // === 응답 처리 ===

    private AiChatResponse processResponse(Long userId, AiResponseContext context) {
        String aiMessage = context.aiMessage();
        ConversationSession session = context.session();
        
        session.addMessage("assistant", aiMessage);

        if (isTransactionComplete(aiMessage)) {
            return handleCompleteTransaction(userId, session, aiMessage, context.book().getBookType());
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

    // === AI API ===

    private AiMessageRequest buildAiRequest(ConversationSession session, BookType bookType) {
        String cachedContentName = promptCacheService.getOrCreateCache(bookType);
        String conversationText = buildConversationText(session);

        return AiMessageRequest.builder()
                .cachedContentName(cachedContentName)
                .conversationText(conversationText)
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
