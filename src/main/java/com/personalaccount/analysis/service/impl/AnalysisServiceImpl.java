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

        // 2. 날짜 계산
        LocalDate startDate = LocalDate.parse(yearMonth + "-01");
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        // 3. 통계 계산
        AnalysisSummary summary = calculateSummary(bookId, startDate, endDate);
        List<CategoryExpense> categoryExpenses = calculateCategoryExpenses(bookId, startDate, endDate);

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
    private AnalysisSummary calculateSummary(
            Long bookId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        BigDecimal totalIncome = transactionRepository
                .findTotalAmountByType(bookId, startDate, endDate, TransactionType.INCOME);

        BigDecimal totalExpense = transactionRepository
                .findTotalAmountByType(bookId, startDate, endDate, TransactionType.EXPENSE);

        // null 체크
        totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        totalExpense = totalExpense != null ? totalExpense : BigDecimal.ZERO;

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
    private List<CategoryExpense> calculateCategoryExpenses(
            Long bookId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<Object[]> results = transactionRepository
                .findCategoryExpensesByBookIdAndDateRange(bookId, startDate, endDate);

        // 총 지출 계산
        BigDecimal totalExpense = results.stream()
                .map(row -> (BigDecimal) row[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // CategoryExpense로 변환
        return results.stream()
                .map(row -> {
                    String categoryName = (String) row[0];
                    BigDecimal amount = (BigDecimal) row[1];

                    Double percentage = totalExpense.compareTo(BigDecimal.ZERO) > 0
                            ? amount.divide(totalExpense, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue()
                            : 0.0;

                    return CategoryExpense.builder()
                            .categoryName(categoryName)
                            .amount(amount)
                            .percentage(percentage)
                            .build();
                })
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