package com.personalaccount.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalaccount.account.repository.AccountRepository;
import com.personalaccount.ai.client.AiClient;
import com.personalaccount.ai.dto.request.AiChatRequest;
import com.personalaccount.ai.dto.request.OpenAiRequest;
import com.personalaccount.ai.dto.response.AiChatResponse;
import com.personalaccount.ai.dto.response.OpenAiResponse;
import com.personalaccount.ai.service.AiChatService;
import com.personalaccount.ai.session.ConversationSession;
import com.personalaccount.ai.session.SessionManager;
import com.personalaccount.book.entity.Book;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @Value("${openai.api.model}")
    private String model;

    @Value("${openai.api.max-tokens}")
    private Integer maxTokens;

    @Value("${openai.api.temperature}")
    private Double temperature;

    @Override
    public AiChatResponse chat(Long userId, AiChatRequest request) {
        log.info("AI 대화 요청: userId={}, bookId={}", userId, request.getBookId());

        // 1. 장부 권한 확인
        validateBookAccess(userId, request.getBookId());

        // 2. 세션 처리 (새 대화 vs 기존 대화)
        ConversationSession session = getOrCreateSession(
                request.getConversationId(),
                userId,
                request.getBookId()
        );

        // 3. 사용자 메시지 저장
        session.addMessage("user", request.getMessage());

        // 4. OpenAI 호출
        OpenAiRequest openAiRequest = buildOpenAiRequest(session);
        OpenAiResponse openAiResponse = aiClient.sendMessage(openAiRequest);

        // 5. AI 응답 추출
        String aiMessage = extractMessage(openAiResponse);
        session.addMessage("assistant", aiMessage);

        // 6. 거래 생성 가능 여부 판단
        if (isTransactionComplete(aiMessage)) {
            return handleCompleteTransaction(userId, session, aiMessage);
        } else {
            return handleMoreInfoNeeded(session, aiMessage);
        }
    }

    /**
     * 세션 조회 또는 생성
     */
    private ConversationSession getOrCreateSession(
            String conversationId,
            Long userId,
            Long bookId
    ) {
        if (conversationId == null) {
            // 새 대화 시작
            return sessionManager.createSession(userId, bookId);
        } else {
            // 기존 대화 이어가기
            ConversationSession session = sessionManager.getSession(conversationId);
            if (session == null) {
                throw new RuntimeException("세션을 찾을 수 없습니다. 다시 시작해주세요.");
            }
            return session;
        }
    }

    /**
     * OpenAI API 요청 생성
     */
    private OpenAiRequest buildOpenAiRequest(ConversationSession session) {
        List<OpenAiRequest.Message> messages = new ArrayList<>();

        // System 프롬프트
        messages.add(OpenAiRequest.Message.builder()
                .role("system")
                .content(getSystemPrompt())
                .build());

        // 대화 내역
        for (ConversationSession.ChatMessage chatMessage : session.getMessages()) {
            messages.add(OpenAiRequest.Message.builder()
                    .role(chatMessage.getRole())
                    .content(chatMessage.getContent())
                    .build());
        }

        return OpenAiRequest.builder()
                .model(model)
                .messages(messages)
                .maxTokens(maxTokens)
                .temperature(temperature)
                .build();
    }

    /**
     * System 프롬프트 (AI 역할 정의)
     */
    private String getSystemPrompt() {
        return """
                당신은 복식부기 가계부 도우미입니다.
                사용자의 수입/지출을 자연어로 입력받아 거래 정보를 추출합니다.
                
                필수 정보:
                1. 거래 타입 (수입/지출)
                2. 금액
                3. 카테고리 (예: 급여, 식비, 교통비)
                4. 결제수단 (예: 현금, 은행, 카드)
                5. 날짜 (생략 시 오늘)
                
                정보가 부족하면 간단히 질문하세요.
                정보가 충분하면 "COMPLETE:" 로 시작하여 JSON 형식으로 응답하세요.
                
                예시:
                - 부족: "어떤 수입인가요?"
                - 충분: "COMPLETE: {"type":"INCOME","amount":50000,"category":"용돈","paymentMethod":"현금","date":"2025-10-18"}"
                """;
    }

    /**
     * OpenAI 응답에서 메시지 추출
     */
    private String extractMessage(OpenAiResponse response) {
        return response.getChoices().get(0).getMessage().getContent();
    }

    /**
     * 거래 생성 가능 여부 판단
     */
    private boolean isTransactionComplete(String message) {
        return message.startsWith("COMPLETE:");
    }

    /**
     * 거래 완료 처리
     */
    @Transactional
    private AiChatResponse handleCompleteTransaction(
            Long userId,
            ConversationSession session,
            String aiMessage
    ) {
        log.info("거래 생성: conversationId={}", session.getConversationId());

        // TODO: JSON 파싱하여 TransactionCreateRequest 생성
        // 지금은 임시로 null 반환
        TransactionCreateRequest transactionRequest = parseTransactionFromAi(aiMessage, session);

        // 거래 생성
        Transaction transaction = transactionService.createTransaction(userId, transactionRequest);
        TransactionResponse transactionResponse = TransactionMapper.toResponse(transaction);

        // 세션 삭제
        sessionManager.deleteSession(session.getConversationId());

        return AiChatResponse.builder()
                .conversationId(null)
                .needsMoreInfo(false)
                .message("거래가 생성되었습니다!")
                .suggestions(null)
                .transaction(transactionResponse)
                .build();
    }

    /**
     * 추가 정보 필요 응답
     */
    private AiChatResponse handleMoreInfoNeeded(
            ConversationSession session,
            String aiMessage
    ) {
        log.debug("추가 정보 필요: conversationId={}", session.getConversationId());

        // 세션 저장
        sessionManager.saveSession(session);

        return AiChatResponse.builder()
                .conversationId(session.getConversationId())
                .needsMoreInfo(true)
                .message(aiMessage)
                .suggestions(null)  // TODO: 선택지 생성
                .transaction(null)
                .build();
    }

    /**
     * AI 응답을 TransactionCreateRequest로 변환
     */
    private TransactionCreateRequest parseTransactionFromAi(
            String aiMessage,
            ConversationSession session
    ) {
        try {
            // "COMPLETE: " 제거
            String jsonString = aiMessage.replace("COMPLETE:", "").trim();

            // JSON 파싱 (Jackson 사용)
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonString);

            // 필수 필드 검증
            if (!jsonNode.has("type") || !jsonNode.has("amount") ||
                    !jsonNode.has("category") || !jsonNode.has("paymentMethod")) {
                throw new RuntimeException("AI 응답에 필수 필드가 누락되었습니다: " + jsonString);
            }

            // 필드 추출
            String type = jsonNode.get("type").asText();
            BigDecimal amount = new BigDecimal(jsonNode.get("amount").asText());
            String categoryName = jsonNode.get("category").asText();
            String paymentMethodName = jsonNode.get("paymentMethod").asText();
            String dateString = jsonNode.has("date") ?
                    jsonNode.get("date").asText() : LocalDate.now().toString();

            // 날짜 변환
            LocalDate date = LocalDate.parse(dateString);

            // 카테고리 ID 조회 (이름 → ID)
            Long categoryId = findAccountIdByName(categoryName);
            Long paymentMethodId = findAccountIdByName(paymentMethodName);

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

    /**
     * 계정과목 이름으로 ID 찾기
     */
    private Long findAccountIdByName(String name) {
        return accountRepository.findByNameAndIsActive(name, true)
                .orElseThrow(() -> new AccountNotFoundException(name))
                .getId();
    }

    /**
     * 장부 접근 권한 확인
     */
    private void validateBookAccess(Long userId, Long bookId) {
        Book book = bookRepository.findByIdAndIsActive(bookId, true)
                .orElseThrow(() -> new BookNotFoundException(bookId));

        if (!book.getUser().getId().equals(userId)) {
            throw new UnauthorizedBookAccessException(bookId);
        }
    }
}