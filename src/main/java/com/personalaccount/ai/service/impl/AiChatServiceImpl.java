package com.personalaccount.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalaccount.account.repository.AccountRepository;
import com.personalaccount.ai.client.AiClient;
import com.personalaccount.ai.dto.request.AiChatRequest;
import com.personalaccount.ai.dto.request.GeminiRequest;
import com.personalaccount.ai.dto.response.AiChatResponse;
import com.personalaccount.ai.dto.response.GeminiResponse;
import com.personalaccount.ai.service.AiChatService;
import com.personalaccount.ai.session.ConversationSession;
import com.personalaccount.ai.session.SessionManager;
import com.personalaccount.book.entity.Book;
import com.personalaccount.book.entity.BookType;
import com.personalaccount.book.repository.BookRepository;
import com.personalaccount.common.exception.custom.AccountNotFoundException;
import com.personalaccount.common.exception.custom.BookNotFoundException;
import com.personalaccount.common.exception.custom.UnauthorizedBookAccessException;
import com.personalaccount.transaction.dto.mapper.TransactionMapper;
import com.personalaccount.transaction.dto.request.TransactionCreateRequest;
import com.personalaccount.transaction.dto.response.TransactionResponse;
import com.personalaccount.transaction.entity.Transaction;
import com.personalaccount.transaction.entity.TransactionType;
import com.personalaccount.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiChatServiceImpl implements AiChatService {

    private final AiClient aiClient;
    private final SessionManager sessionManager;
    private final BookRepository bookRepository;
    private final TransactionService transactionService;
    private final AccountRepository accountRepository;

    @Override
    public AiChatResponse chat(Long userId, AiChatRequest request) {
        log.info("AI 대화 요청: userId={}, bookId={}", userId, request.getBookId());

        // 1. 장부 권한 확인
        validateBookAccess(userId, request.getBookId());

        // 2. 세션 처리
        ConversationSession session = getOrCreateSession(
                request.getConversationId(),
                userId,
                request.getBookId()
        );

        // 3. 사용자 메시지 저장
        session.addMessage("user", request.getMessage());

        // 4. Gemini 호출
        GeminiRequest geminiRequest = buildGeminiRequest(session);
        GeminiResponse geminiResponse = aiClient.sendMessage(geminiRequest);

        // 5. AI 응답 추출
        String aiMessage = extractMessage(geminiResponse);
        session.addMessage("assistant", aiMessage);

        // 6. 거래 생성 가능 여부 판단
        if (isTransactionComplete(aiMessage)) {
            return handleCompleteTransaction(userId, session, aiMessage);
        } else {
            return handleMoreInfoNeeded(session, aiMessage);
        }
    }

    private ConversationSession getOrCreateSession(
            String conversationId,
            Long userId,
            Long bookId
    ) {
        if (conversationId == null || conversationId.isEmpty()) {
            return sessionManager.createSession(userId, bookId);
        } else {
            ConversationSession session = sessionManager.getSession(conversationId);
            if (session == null) {
                throw new RuntimeException("세션을 찾을 수 없습니다. 다시 시작해주세요.");
            }
            return session;
        }
    }

    /**
     * Gemini API 요청 생성
     */
    private GeminiRequest buildGeminiRequest(ConversationSession session) {
        // 대화 내역을 하나의 텍스트로 합치기
        StringBuilder conversationText = new StringBuilder();

        // System 프롬프트
        conversationText.append("당신은 복식부기 가계부 도우미입니다.\n");
        conversationText.append("사용자의 수입/지출을 자연어로 입력받아 거래 정보를 추출합니다.\n\n");
        conversationText.append("필수 정보:\n");
        conversationText.append("1. 거래 타입 (수입/지출)\n");
        conversationText.append("2. 금액\n");
        conversationText.append("3. 카테고리 (예: 급여, 식비, 교통비)\n");
        conversationText.append("4. 결제수단 (예: 현금, 은행, 카드)\n");
        conversationText.append("5. 날짜 (생략 시 오늘)\n\n");
        conversationText.append("정보가 부족하면 간단히 질문하세요.\n");
        conversationText.append("정보가 충분하면 \"COMPLETE:\" 로 시작하여 JSON 형식으로 응답하세요.\n\n");
        conversationText.append("예시:\n");
        conversationText.append("- 부족: \"어떤 수입인가요?\"\n");
        conversationText.append("- 충분: \"COMPLETE: {\\\"type\\\":\\\"INCOME\\\",\\\"amount\\\":50000,\\\"category\\\":\\\"용돈\\\",\\\"paymentMethod\\\":\\\"현금\\\",\\\"date\\\":\\\"2025-10-18\\\"}\"\n\n");
        conversationText.append("---대화 시작---\n\n");

        // 대화 내역 추가
        for (ConversationSession.ChatMessage message : session.getMessages()) {
            conversationText.append(message.getRole()).append(": ").append(message.getContent()).append("\n");
        }

        return GeminiRequest.builder()
                .contents(List.of(
                        GeminiRequest.Content.builder()
                                .parts(List.of(
                                        GeminiRequest.Part.builder()
                                                .text(conversationText.toString())
                                                .build()
                                ))
                                .build()
                ))
                .build();
    }

    /**
     * Gemini 응답에서 메시지 추출
     */
    private String extractMessage(GeminiResponse response) {
        return response.getCandidates().get(0)
                .getContent()
                .getParts().get(0)
                .getText();
    }

    private boolean isTransactionComplete(String message) {
        return message.startsWith("COMPLETE:");
    }

    @Transactional
    private AiChatResponse handleCompleteTransaction(
            Long userId,
            ConversationSession session,
            String aiMessage
    ) {
        log.info("거래 생성: conversationId={}", session.getConversationId());

        TransactionCreateRequest transactionRequest = parseTransactionFromAi(aiMessage, session);

        Transaction transaction = transactionService.createTransaction(userId, transactionRequest);
        TransactionResponse transactionResponse = TransactionMapper.toResponse(transaction);

        sessionManager.deleteSession(session.getConversationId());

        return AiChatResponse.builder()
                .conversationId(null)
                .needsMoreInfo(false)
                .message("거래가 생성되었습니다!")
                .suggestions(null)
                .transaction(transactionResponse)
                .build();
    }

    private AiChatResponse handleMoreInfoNeeded(
            ConversationSession session,
            String aiMessage
    ) {
        log.debug("추가 정보 필요: conversationId={}", session.getConversationId());

        sessionManager.saveSession(session);

        return AiChatResponse.builder()
                .conversationId(session.getConversationId())
                .needsMoreInfo(true)
                .message(aiMessage)
                .suggestions(null)
                .transaction(null)
                .build();
    }

    private TransactionCreateRequest parseTransactionFromAi(
            String aiMessage,
            ConversationSession session
    ) {
        try {
            String jsonString = aiMessage.replace("COMPLETE:", "").trim();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonString);

            if (!jsonNode.has("type") || !jsonNode.has("amount") ||
                    !jsonNode.has("category") || !jsonNode.has("paymentMethod")) {
                throw new RuntimeException("AI 응답에 필수 필드가 누락되었습니다: " + jsonString);
            }

            String type = jsonNode.get("type").asText();
            BigDecimal amount = new BigDecimal(jsonNode.get("amount").asText());
            String categoryName = jsonNode.get("category").asText();
            String paymentMethodName = jsonNode.get("paymentMethod").asText();
            String dateString = jsonNode.has("date") ?
                    jsonNode.get("date").asText() : LocalDate.now().toString();

            LocalDate date = LocalDate.parse(dateString);

            Book book = bookRepository.findByIdAndIsActive(session.getBookId(), true)
                    .orElseThrow(() -> new BookNotFoundException(session.getBookId()));

            BookType bookType = book.getBookType();

            Long categoryId = findAccountIdByName(categoryName, bookType);
            Long paymentMethodId = findAccountIdByName(paymentMethodName, bookType);

            return TransactionCreateRequest.builder()
                    .bookId(session.getBookId())
                    .date(date)
                    .type(TransactionType.valueOf(type))
                    .amount(amount)
                    .categoryId(categoryId)
                    .paymentMethodId(paymentMethodId)
                    .memo("AI 자동 생성")
                    .build();

        } catch (Exception e) {
            log.error("AI 응답 파싱 실패: {}", aiMessage, e);
            throw new RuntimeException("거래 정보를 파싱할 수 없습니다.", e);
        }
    }

    private Long findAccountIdByName(String name, BookType bookType) {
        return accountRepository.findByNameAndBookTypeAndIsActive(name, bookType, true)
                .orElseThrow(() -> new AccountNotFoundException(
                        String.format("계정과목을 찾을 수 없습니다. Name: %s, BookType: %s", name, bookType)
                ))
                .getId();
    }

    private void validateBookAccess(Long userId, Long bookId) {
        Book book = bookRepository.findByIdAndIsActive(bookId, true)
                .orElseThrow(() -> new BookNotFoundException(bookId));

        if (!book.getUser().getId().equals(userId)) {
            throw new UnauthorizedBookAccessException(bookId);
        }
    }
}