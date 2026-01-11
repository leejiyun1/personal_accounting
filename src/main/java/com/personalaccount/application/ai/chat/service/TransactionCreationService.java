package com.personalaccount.application.ai.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalaccount.common.exception.custom.AccountNotFoundException;
import com.personalaccount.common.exception.custom.AiParsingException;
import com.personalaccount.domain.account.repository.AccountRepository;
import com.personalaccount.domain.book.entity.BookType;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionCreationService {

    private final TransactionService transactionService;
    private final AccountRepository accountRepository;
    private final ObjectMapper objectMapper;

    /**
     * AI 응답을 파싱하여 거래 생성
     * - 트랜잭션 범위: 이 메서드만
     */
    @Transactional
    public TransactionResponse createFromAiResponse(
            Long userId,
            Long bookId,
            BookType bookType,
            String aiMessage
    ) {
        log.info("AI 응답으로 거래 생성 - userId: {}, bookId: {}", userId, bookId);

        TransactionCreateRequest request = parseAiMessage(aiMessage, bookId, bookType);
        return transactionService.createTransaction(userId, request);
    }

    private TransactionCreateRequest parseAiMessage(
            String aiMessage,
            Long bookId,
            BookType bookType
    ) {
        try {
            String jsonString = aiMessage.replace("COMPLETE:", "").trim();
            JsonNode jsonNode = objectMapper.readTree(jsonString);

            validateRequiredFields(jsonNode);

            String type = jsonNode.get("type").asText();
            BigDecimal amount = new BigDecimal(jsonNode.get("amount").asText());
            String categoryName = jsonNode.get("category").asText();
            String paymentMethodName = jsonNode.get("paymentMethod").asText();
            LocalDate date = parseDate(jsonNode);
            String memo = parseMemo(jsonNode);

            Long categoryId = findAccountId(categoryName, bookType);
            Long paymentMethodId = findAccountId(paymentMethodName, bookType);

            return TransactionCreateRequest.builder()
                    .bookId(bookId)
                    .date(date)
                    .type(TransactionType.valueOf(type))
                    .amount(amount)
                    .categoryId(categoryId)
                    .paymentMethodId(paymentMethodId)
                    .memo(memo)
                    .build();

        } catch (AiParsingException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI 응답 파싱 실패: {}", aiMessage, e);
            throw new AiParsingException("거래 정보를 파싱할 수 없습니다.", e);
        }
    }

    private void validateRequiredFields(JsonNode jsonNode) {
        if (!jsonNode.has("type") || !jsonNode.has("amount") ||
                !jsonNode.has("category") || !jsonNode.has("paymentMethod")) {
            throw new AiParsingException("AI 응답에 필수 필드가 누락되었습니다");
        }
    }

    private LocalDate parseDate(JsonNode jsonNode) {
        if (jsonNode.has("date") && !jsonNode.get("date").isNull()) {
            return LocalDate.parse(jsonNode.get("date").asText());
        }
        return LocalDate.now();
    }

    private String parseMemo(JsonNode jsonNode) {
        if (jsonNode.has("memo") && !jsonNode.get("memo").isNull()) {
            return jsonNode.get("memo").asText();
        }
        return "AI 자동 생성";
    }

    private Long findAccountId(String name, BookType bookType) {
        return accountRepository.findByNameAndBookTypeAndIsActive(name, bookType, true)
                .orElseThrow(() -> new AccountNotFoundException(
                        String.format("계정과목을 찾을 수 없습니다. Name: %s, BookType: %s", name, bookType)
                ))
                .getId();
    }
}