package com.personalaccount.application.report.service.impl;

import com.personalaccount.application.report.dto.response.*;
import com.personalaccount.application.report.repository.ReportQueryRepository;
import com.personalaccount.application.report.service.ReportService;
import com.personalaccount.common.exception.custom.AccountNotFoundException;
import com.personalaccount.domain.account.entity.AccountType;
import com.personalaccount.domain.account.repository.AccountRepository;
import com.personalaccount.domain.book.entity.Book;
import com.personalaccount.domain.book.repository.BookRepository;
import com.personalaccount.common.exception.custom.BookNotFoundException;
import com.personalaccount.common.exception.custom.UnauthorizedBookAccessException;
import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final ReportQueryRepository reportQueryRepository;
    private final BookRepository bookRepository;
    private final AccountRepository accountRepository;

    @Override
    public List<MonthlySummary> getMonthlySummary(Long userId, Long bookId) {
        log.info("월별 요약 조회: userId={}, bookId={}", userId, bookId);

        validateBookAccess(userId, bookId);

        LocalDate now = LocalDate.now();
        LocalDate startDate = now.minusMonths(5).withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());

        // 6개월 데이터 한 번에 조회
        List<Tuple> results = reportQueryRepository.findIncomeExpenseByDateRange(bookId, startDate, endDate);

        // 월별 집계용 Map (yearMonth -> [income, expense])
        Map<String, BigDecimal[]> dataMap = new LinkedHashMap<>();

        // 6개월 초기화
        for (int i = 5; i >= 0; i--) {
            LocalDate targetMonth = now.minusMonths(i);
            String yearMonth = targetMonth.toString().substring(0, 7);
            dataMap.put(yearMonth, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
        }

        // 데이터 집계
        for (Tuple tuple : results) {
            LocalDate date = tuple.get(0, LocalDate.class);
            AccountType accountType = tuple.get(1, AccountType.class);
            BigDecimal creditAmount = tuple.get(2, BigDecimal.class);
            BigDecimal debitAmount = tuple.get(3, BigDecimal.class);

            if (date == null) continue;
            String yearMonth = date.toString().substring(0, 7);

            BigDecimal[] amounts = dataMap.get(yearMonth);
            if (amounts != null) {
                if (accountType == AccountType.REVENUE) {
                    amounts[0] = amounts[0].add(creditAmount);  // income
                } else if (accountType == AccountType.EXPENSE) {
                    amounts[1] = amounts[1].add(debitAmount);   // expense
                }
            }
        }

        // DTO 변환
        return dataMap.entrySet().stream()
                .map(entry -> {
                    String yearMonth = entry.getKey();
                    BigDecimal income = entry.getValue()[0];
                    BigDecimal expense = entry.getValue()[1];

                    return MonthlySummary.builder()
                            .yearMonth(yearMonth)
                            .income(income)
                            .expense(expense)
                            .balance(income.subtract(expense))
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<CategorySummary> getCategoryStatistics(Long userId, Long bookId, String yearMonth, String type) {
        log.info("카테고리별 통계 조회: userId={}, bookId={}, yearMonth={}, type={}",
                userId, bookId, yearMonth, type);

        validateBookAccess(userId, bookId);

        LocalDate startDate = LocalDate.parse(yearMonth + "-01");
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        List<Tuple> results = reportQueryRepository.findCategoryExpenses(bookId, startDate, endDate);

        // 전체 합계
        BigDecimal totalAmount = results.stream()
                .map(tuple -> tuple.get(1, BigDecimal.class))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // CategorySummary 변환
        return results.stream()
                .map(tuple -> {
                    String name = tuple.get(0, String.class);
                    BigDecimal amount = tuple.get(1, BigDecimal.class);
                    Double percentage = totalAmount.compareTo(BigDecimal.ZERO) > 0 && amount != null
                            ? amount.divide(totalAmount, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue()
                            : 0.0;

                    return CategorySummary.builder()
                            .categoryName(name)
                            .amount(amount)
                            .percentage(percentage)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountBalance> getAccountBalances(Long userId, Long bookId) {
        log.info("계정별 잔액 조회: userId={}, bookId={}", userId, bookId);

        validateBookAccess(userId, bookId);

        // bookType 조회
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("장부를 찾을 수 없습니다"));

        // 해당 장부 타입의 PAYMENT_METHOD 계정만 조회
        List<com.personalaccount.domain.account.entity.Account> accounts =
                accountRepository.findByBookTypeAndAccountTypeAndIsActive(
                        book.getBookType(),
                        com.personalaccount.domain.account.entity.AccountType.PAYMENT_METHOD,
                        true);

        if (accounts.isEmpty()) {
            return Collections.emptyList();
        }

        // 계정 ID 목록 추출
        List<Long> accountIds = accounts.stream()
                .map(com.personalaccount.domain.account.entity.Account::getId)
                .collect(Collectors.toList());

        // 잔액 일괄 조회 (N+1 해결)
        Map<Long, BigDecimal> balanceMap = reportQueryRepository.findAccountBalancesByIds(bookId, accountIds);

        // DTO 변환
        return accounts.stream()
                .map(account -> AccountBalance.builder()
                        .accountId(account.getId())
                        .accountName(account.getName())
                        .balance(balanceMap.getOrDefault(account.getId(), BigDecimal.ZERO))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public FinancialStatement getFinancialStatement(Long userId, Long bookId, String yearMonth) {
        log.info("재무제표 조회: userId={}, bookId={}, yearMonth={}", userId, bookId, yearMonth);

        validateBookAccess(userId, bookId);

        LocalDate startDate = LocalDate.parse(yearMonth + "-01");
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        // 손익계산서
        BigDecimal totalIncome = reportQueryRepository.findTotalIncome(bookId, startDate, endDate);
        BigDecimal totalExpense = reportQueryRepository.findTotalExpense(bookId, startDate, endDate);
        BigDecimal netProfit = totalIncome.subtract(totalExpense);

        // 수익률 계산
        Double profitRate = totalIncome.compareTo(BigDecimal.ZERO) > 0
                ? netProfit.divide(totalIncome, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue()
                : 0.0;

        IncomeStatement incomeStatement = IncomeStatement.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netProfit(netProfit)
                .profitRate(profitRate)
                .build();

        // 재무상태표
        BigDecimal totalAssets = reportQueryRepository.findTotalAssets(bookId, endDate);
        BigDecimal totalLiabilities = reportQueryRepository.findTotalLiabilities(bookId, endDate);
        BigDecimal totalEquity = totalAssets.subtract(totalLiabilities);

        BalanceSheet balanceSheet = BalanceSheet.builder()
                .totalAssets(totalAssets)
                .totalLiabilities(totalLiabilities)
                .totalEquity(totalEquity)
                .build();

        return FinancialStatement.builder()
                .incomeStatement(incomeStatement)
                .balanceSheet(balanceSheet)
                .build();
    }

    @Override
    public Map<String, Object> getAccountLedger(Long userId, Long bookId, Long accountId, String yearMonth) {
        log.info("계정 원장 조회: userId={}, bookId={}, accountId={}, yearMonth={}",
                userId, bookId, accountId, yearMonth);

        validateBookAccess(userId, bookId);

        LocalDate startDate = LocalDate.parse(yearMonth + "-01");
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        // 기초 잔액
        BigDecimal openingBalance = reportQueryRepository.findOpeningBalance(bookId, accountId, startDate);

        // 거래 내역
        List<Tuple> tuples = reportQueryRepository.findAccountLedgerEntries(bookId, accountId, startDate, endDate);

        BigDecimal runningBalance = openingBalance;
        List<TransactionEntry> entries = new ArrayList<>();

        for (Tuple tuple : tuples) {
            LocalDate date = tuple.get(0, LocalDate.class);
            String memo = tuple.get(1, String.class);
            BigDecimal debit = tuple.get(2, BigDecimal.class);
            BigDecimal credit = tuple.get(3, BigDecimal.class);

            runningBalance = runningBalance.add(debit).subtract(credit);

            entries.add(TransactionEntry.builder()
                    .date(date)
                    .description(memo)
                    .debit(debit)
                    .credit(credit)
                    .balance(runningBalance)
                    .build());
        }

        // 계정명 조회
        String accountName = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId))
                .getName();

        Map<String, Object> result = new HashMap<>();
        result.put("accountName", accountName);
        result.put("openingBalance", openingBalance);
        result.put("closingBalance", runningBalance);
        result.put("entries", entries);

        return result;
    }

    @Override
    public Map<String, Object> getAnalysis(Long userId, Long bookId, String yearMonth) {
        log.info("AI 분석 조회: userId={}, bookId={}, yearMonth={}", userId, bookId, yearMonth);

        validateBookAccess(userId, bookId);

        // 재무 요약
        FinancialStatement summary = getFinancialStatement(userId, bookId, yearMonth);

        // 상위 지출 카테고리
        List<CategorySummary> topExpenses = getCategoryStatistics(userId, bookId, yearMonth, "EXPENSE")
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        // AI 코멘트 (TODO: AI 연동)
        AiAnalysisComment aiComment = AiAnalysisComment.builder()
                .overview("분석 준비 중")
                .strengths(List.of())
                .warnings(List.of())
                .suggestions(List.of())
                .build();

        Map<String, Object> result = new HashMap<>();
        result.put("summary", summary);
        result.put("topExpenses", topExpenses);
        result.put("aiComment", aiComment);

        return result;
    }

    private void validateBookAccess(Long userId, Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("장부를 찾을 수 없습니다"));

        if (!book.getUser().getId().equals(userId)) {
            throw new UnauthorizedBookAccessException("해당 장부에 접근 권한이 없습니다");
        }
    }
}