package com.personalaccount.application.ai.chat.service;

import com.personalaccount.application.ai.chat.dto.request.CachedContentRequest;
import com.personalaccount.application.ai.chat.dto.response.CachedContentResponse;
import com.personalaccount.application.ai.util.PromptTemplate;
import com.personalaccount.common.exception.custom.AiServiceException;
import com.personalaccount.domain.account.entity.Account;
import com.personalaccount.domain.account.entity.AccountType;
import com.personalaccount.domain.account.repository.AccountRepository;
import com.personalaccount.domain.ai.client.AiClient;
import com.personalaccount.domain.book.entity.BookType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromptCacheService {

    private static final String CACHE_KEY_PREFIX = "gemini:cache:";
    private static final Duration CACHE_TTL = Duration.ofHours(23);

    private final AiClient aiClient;
    private final AccountRepository accountRepository;
    private final PromptTemplate promptTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 캐시 조회 또는 생성
     */
    public String getOrCreateCache(BookType bookType) {
        String cacheKey = CACHE_KEY_PREFIX + bookType;
        String cachedContentName = redisTemplate.opsForValue().get(cacheKey);

        if (cachedContentName == null) {
            cachedContentName = createCache(bookType, cacheKey);
        }

        return cachedContentName;
    }

    /**
     * 캐시 생성
     * - 읽기 전용 트랜잭션: 계정과목 조회
     * - 외부 API 호출 포함
     */
    @Transactional(readOnly = true)
    public String createCache(BookType bookType, String cacheKey) {
        log.info("캐시 생성 시작 - bookType: {}", bookType);

        String systemPrompt = buildSystemPrompt(bookType);

        // 트랜잭션 종료 후 API 호출하도록 분리하는 게 더 좋지만,
        // 읽기 전용이고 짧은 조회이므로 허용
        CachedContentRequest request = CachedContentRequest.builder()
                .model("models/gemini-2.5-flash")
                .systemInstruction(CachedContentRequest.SystemInstruction.builder()
                        .parts(List.of(CachedContentRequest.Part.builder()
                                .text(systemPrompt)
                                .build()))
                        .build())
                .ttl("82800s")
                .build();

        CachedContentResponse response = aiClient.createCachedContent(request).block();

        if (response == null) {
            throw new AiServiceException("캐시 생성 실패");
        }

        String cachedContentName = response.getName();
        redisTemplate.opsForValue().set(cacheKey, cachedContentName, CACHE_TTL);

        log.info("캐시 생성 완료 - bookType: {}, name: {}", bookType, cachedContentName);

        return cachedContentName;
    }

    private String buildSystemPrompt(BookType bookType) {
        List<String> incomeCategories = getAccountNames(bookType, AccountType.REVENUE);
        List<String> expenseCategories = getAccountNames(bookType, AccountType.EXPENSE);
        List<String> paymentMethods = getAccountNames(bookType, AccountType.PAYMENT_METHOD);

        return promptTemplate.loadTemplate(
                "prompts/transaction-prompt.txt",
                Map.of(
                        "TODAY", LocalDate.now().toString(),
                        "INCOME_CATEGORIES", String.join(", ", incomeCategories),
                        "EXPENSE_CATEGORIES", String.join(", ", expenseCategories),
                        "PAYMENTS", String.join(", ", paymentMethods)
                )
        );
    }

    private List<String> getAccountNames(BookType bookType, AccountType accountType) {
        return accountRepository
                .findByBookTypeAndAccountTypeAndIsActive(bookType, accountType, true)
                .stream()
                .map(Account::getName)
                .sorted()
                .toList();
    }
}