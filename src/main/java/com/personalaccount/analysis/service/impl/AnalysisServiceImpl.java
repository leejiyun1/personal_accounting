package com.personalaccount.analysis.service.impl;

import com.personalaccount.analysis.dto.response.AiAnalysisComment;
import com.personalaccount.analysis.dto.response.AnalysisResponse;
import com.personalaccount.analysis.dto.response.AnalysisSummary;
import com.personalaccount.analysis.dto.response.CategoryExpense;
import com.personalaccount.analysis.service.AnalysisService;
import com.personalaccount.book.entity.Book;
import com.personalaccount.book.repository.BookRepository;
import com.personalaccount.common.exception.custom.BookNotFoundException;
import com.personalaccount.common.exception.custom.UnauthorizedBookAccessException;
import com.personalaccount.transaction.entity.Transaction;
import com.personalaccount.transaction.entity.TransactionType;
import com.personalaccount.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisServiceImpl implements AnalysisService {

    private final BookRepository bookRepository;
    private final TransactionRepository transactionRepository;
    // private final AiClient aiClient; // TODO: AI 분석 추가

    @Override
    public AnalysisResponse getAnalysis(Long userId, Long bookId, String yearMonth) {
        log.info("경영 분석 조회: userId={}, bookId={}, yearMonth={}", userId, bookId, yearMonth);

        // 1. 장부 권한 확인
        validateBookAccess(userId, bookId);

        // 2. 해당 월의 거래 조회
        LocalDate startDate = LocalDate.parse(yearMonth + "-01");
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        List<Transaction> transactions = transactionRepository
                .findByBookIdAndDateBetween(bookId, startDate, endDate);

        // 3. 통계 계산
        AnalysisSummary summary = calculateSummary(transactions);
        List<CategoryExpense> categoryExpenses = calculateCategoryExpenses(transactions);

        // 4. AI 분석 (TODO: 실제 AI 호출로 교체)
        AiAnalysisComment aiAnalysis = generateAiAnalysis(summary, categoryExpenses);

        return AnalysisResponse.builder()
                .summary(summary)
                .aiAnalysis(aiAnalysis)
                .categoryExpenses(categoryExpenses)
                .build();
    }

    /**
     * 통계 요약 계산
     */
    private AnalysisSummary calculateSummary(List<Transaction> transactions) {
        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netProfit = totalIncome.subtract(totalExpense);

        Double profitRate = totalIncome.compareTo(BigDecimal.ZERO) > 0
                ? netProfit.divide(totalIncome, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;

        return AnalysisSummary.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netProfit(netProfit)
                .profitRate(profitRate)
                .build();
    }

    /**
     * 카테고리별 지출 계산
     */
    private List<CategoryExpense> calculateCategoryExpenses(List<Transaction> transactions) {
        BigDecimal totalExpense = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> categoryMap = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getName(),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        return categoryMap.entrySet().stream()
                .map(entry -> {
                    Double percentage = totalExpense.compareTo(BigDecimal.ZERO) > 0
                            ? entry.getValue().divide(totalExpense, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue()
                            : 0.0;

                    return CategoryExpense.builder()
                            .categoryName(entry.getKey())
                            .amount(entry.getValue())
                            .percentage(percentage)
                            .build();
                })
                .sorted((a, b) -> b.getAmount().compareTo(a.getAmount())) // 금액 내림차순
                .limit(5)  // TOP 5
                .collect(Collectors.toList());
    }

    /**
     * AI 분석 생성 (임시 더미)
     * TODO: 실제 Gemini API 호출로 교체
     */
    private AiAnalysisComment generateAiAnalysis(
            AnalysisSummary summary,
            List<CategoryExpense> categoryExpenses
    ) {
        return AiAnalysisComment.builder()
                .overview("이번 달 순이익은 " + summary.getNetProfit() + "원입니다.")
                .strengths(List.of("수익률이 양호합니다"))
                .warnings(List.of("지출이 많습니다"))
                .suggestions(List.of("지출을 줄여보세요"))
                .build();
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