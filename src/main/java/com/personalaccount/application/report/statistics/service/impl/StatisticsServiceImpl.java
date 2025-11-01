package com.personalaccount.application.report.statistics.service.impl;

import com.personalaccount.domain.book.entity.Book;
import com.personalaccount.domain.book.repository.BookRepository;
import com.personalaccount.common.exception.custom.BookNotFoundException;
import com.personalaccount.common.exception.custom.UnauthorizedBookAccessException;
import com.personalaccount.application.report.statistics.dto.CategoryStatistics;
import com.personalaccount.application.report.statistics.dto.MonthlySummary;
import com.personalaccount.application.report.statistics.dto.response.AccountBalanceResponse;
import com.personalaccount.application.report.statistics.dto.AccountBalance;
import com.personalaccount.application.report.statistics.dto.response.CategoryStatisticsResponse;
import com.personalaccount.application.report.statistics.dto.response.MonthlySummaryResponse;
import com.personalaccount.application.report.statistics.repository.StatisticsRepository;
import com.personalaccount.application.report.statistics.service.StatisticsService;
import com.personalaccount.domain.transaction.entity.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsRepository statisticsRepository;
    private final BookRepository bookRepository;

    @Override
    public List<MonthlySummaryResponse> getMonthlySummary(Long userId, Long bookId) {
        log.info("월별 요약 조회: userId={}, bookId={}", userId, bookId);

        // 1. 장부 권한 확인
        validateBookAccess(userId, bookId);

        // 2. 최근 6개월 시작일 계산 (예: 2025-05-01)
        LocalDate startDate = LocalDate.now()
                .minusMonths(5)
                .withDayOfMonth(1);

        // 3. Repository 호출
        List<MonthlySummary> summaries = statisticsRepository.getMonthlySummary(bookId, startDate);

        // 4. DTO 변환 (balance 계산)
        return summaries.stream()
                .map(summary -> MonthlySummaryResponse.builder()
                        .yearMonth(summary.getYearMonth())
                        .totalIncome(summary.getTotalIncome())
                        .totalExpense(summary.getTotalExpense())
                        .balance(summary.getTotalIncome().subtract(summary.getTotalExpense()))
                        .build())
                .toList();
    }

    @Override
    public CategoryStatisticsResponse getCategoryStatistics(
            Long userId,
            Long bookId,
            String yearMonth,
            TransactionType type
    ) {
        log.info("카테고리별 통계 조회: userId={}, bookId={}, yearMonth={}, type={}",
                userId, bookId, yearMonth, type);

        // 1. 장부 권한 확인
        validateBookAccess(userId, bookId);

        // 2. YearMonth 변환
        YearMonth ym = YearMonth.parse(yearMonth);

        // 3. Repository 호출 (수입/지출 분기)
        List<CategoryStatistics> statistics = (type == TransactionType.INCOME)
                ? statisticsRepository.getIncomeCategoryStatistics(bookId, ym)
                : statisticsRepository.getExpenseCategoryStatistics(bookId, ym);

        // 4. 전체 합계 계산
        BigDecimal totalAmount = statistics.stream()
                .map(CategoryStatistics::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5. 카테고리 목록 변환 (percentage 계산)
        List<CategoryStatisticsResponse.CategoryItem> items = statistics.stream()
                .map(stat -> {
                    // percentage = (개별 금액 / 전체 금액) * 100
                    Double percentage = totalAmount.compareTo(BigDecimal.ZERO) > 0
                            ? stat.getAmount()
                            .divide(totalAmount, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue()
                            : 0.0;

                    return CategoryStatisticsResponse.CategoryItem.builder()
                            .categoryId(stat.getCategoryId())
                            .categoryCode(stat.getCategoryCode())
                            .categoryName(stat.getCategoryName())
                            .amount(stat.getAmount())
                            .percentage(percentage)
                            .build();
                })
                .toList();

        // 6. 전체 응답 생성
        return CategoryStatisticsResponse.builder()
                .yearMonth(yearMonth)
                .type(type.name())
                .totalAmount(totalAmount)
                .categories(items)
                .build();
    }
    @Override
    public List<AccountBalanceResponse> getAccountBalances(Long userId, Long bookId) {
        log.info("계정별 잔액 조회: userId={}, bookId={}", userId, bookId);

        // 1. 장부 권한 확인
        validateBookAccess(userId, bookId);

        // 2. Repository 호출
        List<AccountBalance> balances = statisticsRepository.getAccountBalances(bookId);

        // 3. DTO 변환
        return balances.stream()
                .map(balance -> AccountBalanceResponse.builder()
                        .accountId(balance.getAccountId())
                        .accountName(balance.getAccountName())
                        .balance(balance.getBalance().longValue())
                        .build())
                .toList();
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