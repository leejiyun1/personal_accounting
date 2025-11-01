package com.personalaccount.application.report.ledger.service.impl;

import com.personalaccount.domain.account.repository.AccountRepository;
import com.personalaccount.domain.book.entity.Book;
import com.personalaccount.domain.book.repository.BookRepository;
import com.personalaccount.common.exception.custom.BookNotFoundException;
import com.personalaccount.common.exception.custom.UnauthorizedBookAccessException;
import com.personalaccount.application.report.ledger.dto.response.AccountLedgerResponse;
import com.personalaccount.application.report.ledger.dto.response.BalanceSheet;
import com.personalaccount.application.report.ledger.dto.response.IncomeStatement;
import com.personalaccount.application.report.ledger.dto.response.LedgerSummaryResponse;
import com.personalaccount.application.report.ledger.repository.LedgerRepository;
import com.personalaccount.application.report.ledger.service.LedgerService;
import com.personalaccount.domain.transaction.entity.TransactionType;
import com.personalaccount.domain.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class LedgerServiceImpl implements LedgerService {

    private final BookRepository bookRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerRepository ledgerRepository;
    private final AccountRepository accountRepository;

    @Override
    public LedgerSummaryResponse getLedgerSummary(Long userId, Long bookId, String yearMonth) {
        log.info("재무 요약 조회: userId={}, bookId={}, yearMonth={}", userId, bookId, yearMonth);

        // 1. 장부 권한 확인
        validateBookAccess(userId, bookId);

        // 2. 날짜 계산
        LocalDate startDate = LocalDate.parse(yearMonth + "-01");
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        // 3. 손익계산서 계산
        IncomeStatement incomeStatement = calculateIncomeStatement(bookId, startDate, endDate);

        // 4. 재무상태표 계산
        BalanceSheet balanceSheet = calculateBalanceSheet(bookId, endDate);

        return LedgerSummaryResponse.builder()
                .incomeStatement(incomeStatement)
                .balanceSheet(balanceSheet)
                .build();
    }

    @Override
    public AccountLedgerResponse getAccountLedger(Long userId, Long bookId, Long accountId, String yearMonth) {
        log.info("계정별 원장 조회: userId={}, bookId={}, accountId={}, yearMonth={}",
                userId, bookId, accountId, yearMonth);

        // 1. 장부 권한 확인
        validateBookAccess(userId, bookId);

        // 2. 날짜 계산
        LocalDate startDate = LocalDate.parse(yearMonth + "-01");
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        // 3. 계정과목명 조회
        String accountName = accountRepository.findById(accountId)
                .map(account -> account.getName())
                .orElse("알 수 없는 계정");

        // 4. 기초 잔액 계산
        BigDecimal openingBalance = calculateOpeningBalance(bookId, accountId, startDate);

        // 5. 거래 내역 조회 및 잔액 계산
        List<AccountLedgerResponse.LedgerEntry> entries = calculateLedgerEntries(
                bookId, accountId, startDate, endDate, openingBalance
        );

        // 6. 기말 잔액
        BigDecimal closingBalance = entries.isEmpty()
                ? openingBalance
                : entries.get(entries.size() - 1).getBalance();

        return AccountLedgerResponse.builder()
                .accountName(accountName)
                .openingBalance(openingBalance)
                .closingBalance(closingBalance)
                .entries(entries)
                .build();
    }

    /**
     * 손익계산서 계산
     */
    private IncomeStatement calculateIncomeStatement(
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

        BigDecimal netIncome = totalIncome.subtract(totalExpense);

        return IncomeStatement.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netIncome(netIncome)
                .build();
    }

    /**
     * 재무상태표 계산
     */
    private BalanceSheet calculateBalanceSheet(Long bookId, LocalDate asOfDate) {
        BigDecimal totalAssets = ledgerRepository.findTotalAssets(bookId, asOfDate);
        BigDecimal totalLiabilities = ledgerRepository.findTotalLiabilities(bookId, asOfDate);

        // null 체크
        totalAssets = totalAssets != null ? totalAssets : BigDecimal.ZERO;
        totalLiabilities = totalLiabilities != null ? totalLiabilities : BigDecimal.ZERO;

        // 자본 = 자산 - 부채
        BigDecimal totalEquity = totalAssets.subtract(totalLiabilities);

        return BalanceSheet.builder()
                .totalAssets(totalAssets)
                .totalLiabilities(totalLiabilities)
                .totalEquity(totalEquity)
                .build();
    }

    /**
     * 기초 잔액 계산
     */
    private BigDecimal calculateOpeningBalance(Long bookId, Long accountId, LocalDate startDate) {
        BigDecimal balance = ledgerRepository.findOpeningBalance(bookId, accountId, startDate);
        return balance != null ? balance : BigDecimal.ZERO;
    }

    /**
     * 계정별 거래 내역 및 잔액 계산
     */
    private List<AccountLedgerResponse.LedgerEntry> calculateLedgerEntries(
            Long bookId,
            Long accountId,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal openingBalance
    ) {
        List<Object[]> results = ledgerRepository.findAccountLedgerEntries(
                bookId, accountId, startDate, endDate
        );

        List<AccountLedgerResponse.LedgerEntry> entries = new ArrayList<>();
        BigDecimal runningBalance = openingBalance;

        for (Object[] row : results) {
            LocalDate date = (LocalDate) row[0];
            String description = (String) row[1];
            BigDecimal debit = (BigDecimal) row[2];
            BigDecimal credit = (BigDecimal) row[3];

            // null 체크
            debit = debit != null ? debit : BigDecimal.ZERO;
            credit = credit != null ? credit : BigDecimal.ZERO;

            // 잔액 계산 (차변 증가, 대변 감소)
            runningBalance = runningBalance.add(debit).subtract(credit);

            entries.add(
                    AccountLedgerResponse.LedgerEntry.builder()
                            .date(date)
                            .description(description)
                            .debit(debit)
                            .credit(credit)
                            .balance(runningBalance)
                            .build()
            );
        }

        return entries;
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