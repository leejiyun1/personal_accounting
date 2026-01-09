package com.personalaccount.application.ai.chat.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalaccount.application.ai.chat.dto.request.AiChatRequest;
import com.personalaccount.application.ai.chat.dto.request.GeminiRequest;
import com.personalaccount.application.ai.chat.dto.response.AiChatResponse;
import com.personalaccount.application.ai.chat.dto.response.GeminiResponse;
import com.personalaccount.application.ai.chat.service.AiChatService;
import com.personalaccount.application.ai.session.ConversationSession;
import com.personalaccount.application.ai.util.PromptTemplate;
import com.personalaccount.common.exception.custom.AccountNotFoundException;
import com.personalaccount.common.exception.custom.AiParsingException;
import com.personalaccount.common.exception.custom.AiServiceException;
import com.personalaccount.common.exception.custom.BookNotFoundException;
import com.personalaccount.common.exception.custom.SessionNotFoundException;
import com.personalaccount.common.exception.custom.UnauthorizedBookAccessException;
import com.personalaccount.domain.account.entity.Account;
import com.personalaccount.domain.account.entity.AccountType;
import com.personalaccount.domain.account.repository.AccountRepository;
import com.personalaccount.domain.ai.client.AiClient;
import com.personalaccount.domain.ai.repository.SessionRepository;
import com.personalaccount.domain.book.entity.Book;
import com.personalaccount.domain.book.entity.BookType;
import com.personalaccount.domain.book.repository.BookRepository;
import com.personalaccount.domain.transaction.dto.request.TransactionCreateRequest;
import com.personalaccount.domain.transaction.dto.response.TransactionResponse;
import com.personalaccount.domain.transaction.entity.TransactionType;
import com.personalaccount.domain.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiChatServiceImpl implements AiChatService {

    private final AiClient aiClient;
    private final SessionRepository sessionRepository;
    private final BookRepository bookRepository;
    private final TransactionService transactionService;
    private final AccountRepository accountRepository;
    private final PromptTemplate promptTemplate;

    @Override
    @Transactional
    public AiChatResponse chat(Long userId, AiChatRequest request) {
        log.info("AI 대화 요청: userId={}, bookId={}", userId, request.getBookId());

        validateBookAccess(userId, request.getBookId());

        ConversationSession session = getOrCreateSession(
                request.getConversationId(),
                userId,
                request.getBookId()
        );

        session.addMessage("user", request.getMessage());

        GeminiRequest geminiRequest = buildGeminiRequest(session);

        GeminiResponse geminiResponse = aiClient.sendMessage(geminiRequest)
                .block();

        if (geminiResponse == null) {
            throw new AiServiceException("AI 응답이 비어있습니다");
        }

        // 토큰 사용량 로깅
        if (geminiResponse.getUsageMetadata() != null) {
            log.info("대화 세션 {} - 토큰 사용: {}",
                    session.getConversationId(),
                    geminiResponse.getUsageMetadata().getTotalTokenCount());
        }

        String aiMessage = extractMessage(geminiResponse);
        session.addMessage("assistant", aiMessage);

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
            return sessionRepository.createSession(userId, bookId);
        } else {
            ConversationSession session = sessionRepository.getSession(conversationId);
            if (session == null) {
                throw new SessionNotFoundException("세션을 찾을 수 없습니다. conversationId: " + conversationId);
            }
            return session;
        }
    }

    private AiChatResponse handleCompleteTransaction(
            Long userId,
            ConversationSession session,
            String aiMessage
    ) {
        log.info("거래 생성: conversationId={}", session.getConversationId());

        TransactionCreateRequest transactionRequest = parseTransactionFromAi(aiMessage, session);

        TransactionResponse transactionResponse = transactionService.createTransaction(userId, transactionRequest);

        sessionRepository.deleteSession(session.getConversationId());

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

        sessionRepository.saveSession(session);

        return AiChatResponse.builder()
                .conversationId(session.getConversationId())
                .needsMoreInfo(true)
                .message(aiMessage)
                .suggestions(null)
                .transaction(null)
                .build();
    }

    private GeminiRequest buildGeminiRequest(ConversationSession session) {
        // System Instruction (캐싱됨)
        String systemPrompt = buildSystemPrompt(session.getBookId());

        // 대화 내용만 추가
        StringBuilder conversation = new StringBuilder();
        for (ConversationSession.ChatMessage message : session.getMessages()) {
            conversation.append(message.getRole())
                    .append(": ")
                    .append(message.getContent())
                    .append("\n");
        }

        return GeminiRequest.builder()
                .systemInstruction(GeminiRequest.SystemInstruction.builder()
                        .parts(List.of(GeminiRequest.Part.builder()
                                .text(systemPrompt)
                                .build()))
                        .build())
                .contents(List.of(GeminiRequest.Content.builder()
                        .parts(List.of(GeminiRequest.Part.builder()
                                .text(conversation.toString())
                                .build()))
                        .build()))
                .build();
    }

    private String buildSystemPrompt(Long bookId) {
        Book book = bookRepository.findByIdAndIsActive(bookId, true)
                .orElseThrow(() -> new BookNotFoundException(bookId));

        BookType bookType = book.getBookType();

        // 수입 카테고리
        List<String> incomeCategories = accountRepository
                .findByBookTypeAndAccountTypeAndIsActive(bookType, AccountType.REVENUE, true)
                .stream()
                .map(Account::getName)
                .sorted()
                .toList();

        // 지출 카테고리
        List<String> expenseCategories = accountRepository
                .findByBookTypeAndAccountTypeAndIsActive(bookType, AccountType.EXPENSE, true)
                .stream()
                .map(Account::getName)
                .sorted()
                .toList();

        // 결제수단
        List<String> payments = accountRepository
                .findByBookTypeAndAccountTypeAndIsActive(bookType, AccountType.PAYMENT_METHOD, true)
                .stream()
                .map(Account::getName)
                .sorted()
                .toList();

        return promptTemplate.loadTemplate(
                "prompts/transaction-prompt.txt",
                Map.of(
                        "TODAY", LocalDate.now().toString(),
                        "INCOME_CATEGORIES", String.join(", ", incomeCategories),
                        "EXPENSE_CATEGORIES", String.join(", ", expenseCategories),
                        "PAYMENTS", String.join(", ", payments)
                )
        );
    }

    private String extractMessage(GeminiResponse response) {
        return response.getCandidates().getFirst()
                .getContent()
                .getParts().getFirst()
                .getText();
    }

    private boolean isTransactionComplete(String message) {
        return message.startsWith("COMPLETE:");
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
                throw new AiParsingException("AI 응답에 필수 필드가 누락되었습니다: " + jsonString);
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

        } catch (AiParsingException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI 응답 파싱 실패: {}", aiMessage, e);
            throw new AiParsingException("거래 정보를 파싱할 수 없습니다.", e);
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