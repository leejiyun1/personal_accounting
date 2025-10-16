package com.personalaccount.transaction.service.impl;

import com.personalaccount.account.entity.Account;
import com.personalaccount.account.repository.AccountRepository;
import com.personalaccount.book.entity.Book;
import com.personalaccount.book.repository.BookRepository;
import com.personalaccount.common.exception.custom.*;
import com.personalaccount.transaction.dto.request.TransactionCreateRequest;
import com.personalaccount.transaction.dto.request.TransactionUpdateRequest;
import com.personalaccount.transaction.entity.*;
import com.personalaccount.transaction.repository.JournalEntryRepository;
import com.personalaccount.transaction.repository.TransactionDetailRepository;
import com.personalaccount.transaction.repository.TransactionRepository;
import com.personalaccount.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final TransactionDetailRepository transactionDetailRepository;
    private final BookRepository bookRepository;
    private final AccountRepository accountRepository;

    /**
     * 거래 생성 + 복식부기 자동 처리
     */
    @Transactional
    @Override
    public Transaction createTransaction(Long userId, TransactionCreateRequest request) {
        log.info("거래 생성 요청: userId={}, bookId={}, type={}, amount={}",
                userId, request.getBookId(), request.getType(), request.getAmount());

        // 1. 장부 조회 및 권한 확인
        Book book = bookRepository.findByIdAndIsActive(request.getBookId(), true)
                .orElseThrow(() -> new BookNotFoundException(request.getBookId()));

        if (!book.getUser().getId().equals(userId)) {
            throw new UnauthorizedBookAccessException(request.getBookId());
        }

        // 2. 계정과목 조회
        Account category = accountRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AccountNotFoundException(request.getCategoryId()));

        Account paymentMethod = accountRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new AccountNotFoundException(request.getPaymentMethodId()));

        // 3. Transaction 생성
        Transaction transaction = Transaction.builder()
                .book(book)
                .date(request.getDate())
                .type(request.getType())
                .memo(request.getMemo())
                .build();
        Transaction savedTransaction = transactionRepository.save(transaction);

        // 4. JournalEntry 생성
        String description = generateDescription(request.getType(), category.getName(), request.getAmount());
        JournalEntry journalEntry = JournalEntry.builder()
                .transaction(savedTransaction)
                .date(request.getDate())
                .description(description)
                .build();
        JournalEntry savedJournalEntry = journalEntryRepository.save(journalEntry);

        // 5. 복식부기 처리 (차변/대변)
        createDoubleEntryDetails(savedJournalEntry, request.getType(), category, paymentMethod, request.getAmount());

        log.info("거래 생성 완료: transactionId={}", savedTransaction.getId());

        return savedTransaction;
    }

    /**
     * 복식부기 상세 내역 생성
     */
    private void createDoubleEntryDetails(
            JournalEntry journalEntry,
            TransactionType type,
            Account category,
            Account paymentMethod,
            BigDecimal amount
    ) {
        if (type == TransactionType.INCOME) {
            // 수입: 차변(결제수단), 대변(수입카테고리)
            createDetail(journalEntry, paymentMethod, DetailType.DEBIT, amount);
            createDetail(journalEntry, category, DetailType.CREDIT, amount);
        } else {
            // 지출: 차변(지출카테고리), 대변(결제수단)
            createDetail(journalEntry, category, DetailType.DEBIT, amount);
            createDetail(journalEntry, paymentMethod, DetailType.CREDIT, amount);
        }
    }

    /**
     * TransactionDetail 생성 헬퍼
     */
    private void createDetail(
            JournalEntry journalEntry,
            Account account,
            DetailType detailType,
            BigDecimal amount
    ) {
        TransactionDetail detail = TransactionDetail.builder()
                .journalEntry(journalEntry)
                .account(account)
                .detailType(detailType)
                .debitAmount(detailType == DetailType.DEBIT ? amount : BigDecimal.ZERO)
                .creditAmount(detailType == DetailType.CREDIT ? amount : BigDecimal.ZERO)
                .build();

        transactionDetailRepository.save(detail);
    }

    /**
     * 분개 설명 생성
     */
    private String generateDescription(TransactionType type, String categoryName, BigDecimal amount) {
        return String.format("%s - %s %s원",
                type == TransactionType.INCOME ? "수입" : "지출",
                categoryName,
                amount.toString());
    }

    @Override
    public List<Transaction> getTransactions(Long userId, Long bookId) {
        log.debug("거래 목록 조회: userId={}, bookId={}", userId, bookId);

        validateBookAccess(userId, bookId);

        return transactionRepository.findByBookIdAndIsActiveOrderByDateDesc(bookId, true);
    }

    @Override
    public List<Transaction> getTransactionsByType(Long userId, Long bookId, TransactionType type) {
        log.debug("거래 목록 조회(타입별): userId={}, bookId={}, type={}", userId, bookId, type);

        validateBookAccess(userId, bookId);

        return transactionRepository.findByBookIdAndTypeAndIsActiveOrderByDateDesc(bookId, type, true);
    }

    @Override
    public List<Transaction> getTransactionsByDateRange(
            Long userId,
            Long bookId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        log.debug("거래 목록 조회(기간별): userId={}, bookId={}, start={}, end={}",
                userId, bookId, startDate, endDate);

        validateBookAccess(userId, bookId);

        return transactionRepository.findByBookIdAndDateBetweenAndIsActiveOrderByDateDesc(
                bookId, startDate, endDate, true);
    }

    @Override
    public Transaction getTransaction(Long userId, Long id) {
        log.debug("거래 상세 조회: userId={}, transactionId={}", userId, id);

        Transaction transaction = transactionRepository.findByIdAndIsActive(id, true)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        validateBookAccess(userId, transaction.getBook().getId());

        return transaction;
    }

    @Transactional
    @Override
    public Transaction updateTransaction(Long userId, Long id, TransactionUpdateRequest request) {
        log.info("거래 수정 요청: userId={}, transactionId={}", userId, id);

        Transaction transaction = getTransaction(userId, id);

        if (request.getMemo() != null) {
            transaction.updateMemo(request.getMemo());
        }

        log.info("거래 수정 완료: transactionId={}", id);

        return transaction;
    }

    @Transactional
    @Override
    public void deleteTransaction(Long userId, Long id) {
        log.info("거래 삭제 요청: userId={}, transactionId={}", userId, id);

        Transaction transaction = getTransaction(userId, id);
        transaction.deactivate();

        log.info("거래 삭제 완료: transactionId={}", id);
    }

    /**
     * 장부 접근 권한 검증
     */
    private void validateBookAccess(Long userId, Long bookId) {
        Book book = bookRepository.findByIdAndIsActive(bookId, true)
                .orElseThrow(() -> new BookNotFoundException(bookId));

        if (!book.getUser().getId().equals(userId)) {
            throw new UnauthorizedBookAccessException(bookId);
        }
    }
}