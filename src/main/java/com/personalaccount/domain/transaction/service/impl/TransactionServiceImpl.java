package com.personalaccount.domain.transaction.service.impl;

import com.personalaccount.domain.account.entity.Account;
import com.personalaccount.domain.account.entity.AccountType;
import com.personalaccount.domain.account.repository.AccountRepository;
import com.personalaccount.domain.book.entity.Book;
import com.personalaccount.domain.book.repository.BookRepository;
import com.personalaccount.common.exception.custom.*;
import com.personalaccount.domain.transaction.dto.request.TransactionCreateRequest;
import com.personalaccount.domain.transaction.dto.request.TransactionSearchCondition;
import com.personalaccount.domain.transaction.dto.request.TransactionUpdateRequest;
import com.personalaccount.domain.transaction.entity.*;
import com.personalaccount.domain.transaction.repository.JournalEntryRepository;
import com.personalaccount.domain.transaction.repository.TransactionDetailRepository;
import com.personalaccount.domain.transaction.repository.TransactionRepository;
import com.personalaccount.domain.transaction.service.TransactionService;
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

    @Transactional
    @Override
    public Transaction createTransaction(Long userId, TransactionCreateRequest request) {
        log.info("거래 생성 요청: userId={}, bookId={}, type={}, amount={}",
                userId, request.getBookId(), request.getType(), request.getAmount());

        Book book = bookRepository.findByIdAndIsActive(request.getBookId(), true)
                .orElseThrow(() -> new BookNotFoundException(request.getBookId()));

        if (!book.getUser().getId().equals(userId)) {
            throw new UnauthorizedBookAccessException(request.getBookId());
        }

        Account category = accountRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AccountNotFoundException(request.getCategoryId()));

        Account paymentMethod = accountRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new AccountNotFoundException(request.getPaymentMethodId()));

        validateAccountTypes(request.getType(), category, paymentMethod);
        validateBookTypes(book, category, paymentMethod);

        Transaction transaction = Transaction.builder()
                .book(book)
                .date(request.getDate())
                .type(request.getType())
                .memo(request.getMemo())
                .build();
        Transaction savedTransaction = transactionRepository.save(transaction);

        String description = generateDescription(request.getType(), category.getName(), request.getAmount());
        JournalEntry journalEntry = JournalEntry.builder()
                .transaction(savedTransaction)
                .date(request.getDate())
                .description(description)
                .build();
        JournalEntry savedJournalEntry = journalEntryRepository.save(journalEntry);

        createDoubleEntryDetails(savedJournalEntry, request.getType(), category, paymentMethod, request.getAmount());

        log.info("거래 생성 완료: transactionId={}", savedTransaction.getId());

        return savedTransaction;
    }

    @Override
    public List<Transaction> getTransactions(
            Long userId,
            Long bookId,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate) {

        log.debug("거래 목록 조회: userId={}, bookId={}, type={}, start={}, end={}",
                userId, bookId, type, startDate, endDate);

        validateBookAccess(userId, bookId);

        TransactionSearchCondition condition = TransactionSearchCondition.builder()
                .bookId(bookId)
                .type(type)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        return transactionRepository.searchTransactions(condition);
    }

    @Override
    public Transaction getTransaction(Long userId, Long id) {
        log.debug("거래 상세 조회: userId={}, transactionId={}", userId, id);

        Transaction transaction = transactionRepository.findByIdAndIsActive(id, true)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        if (!transaction.getBook().getUser().getId().equals(userId)) {
            throw new UnauthorizedBookAccessException(transaction.getBook().getId());
        }

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

    private void createDoubleEntryDetails(
            JournalEntry journalEntry,
            TransactionType type,
            Account category,
            Account paymentMethod,
            BigDecimal amount) {

        if (type == TransactionType.INCOME) {
            createDetail(journalEntry, paymentMethod, DetailType.DEBIT, amount);
            createDetail(journalEntry, category, DetailType.CREDIT, amount);
        } else {
            createDetail(journalEntry, category, DetailType.DEBIT, amount);
            createDetail(journalEntry, paymentMethod, DetailType.CREDIT, amount);
        }

        validateDoubleEntry(journalEntry);
    }

    private void validateDoubleEntry(JournalEntry journalEntry) {
        List<TransactionDetail> details = transactionDetailRepository
                .findByJournalEntryId(journalEntry.getId());

        BigDecimal debitSum = details.stream()
                .map(TransactionDetail::getDebitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal creditSum = details.stream()
                .map(TransactionDetail::getCreditAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (debitSum.compareTo(creditSum) != 0) {
            throw new InvalidTransactionException(
                    String.format("차변과 대변 합계가 일치하지 않습니다. 차변: %s, 대변: %s",
                            debitSum, creditSum)
            );
        }
    }

    private void validateAccountTypes(
            TransactionType transactionType,
            Account category,
            Account paymentMethod) {

        if (transactionType == TransactionType.INCOME) {
            if (category.getAccountType() != AccountType.REVENUE) {
                throw new InvalidTransactionException(
                        "수입 거래는 수익 계정과목을 사용해야 합니다.");
            }
        } else {
            if (category.getAccountType() != AccountType.EXPENSE) {
                throw new InvalidTransactionException(
                        "지출 거래는 비용 계정과목을 사용해야 합니다.");
            }
        }

        if (paymentMethod.getAccountType() != AccountType.PAYMENT_METHOD) {
            throw new InvalidTransactionException(
                    "결제수단으로 올바른 계정과목을 선택해야 합니다.");
        }
    }

    private void validateBookTypes(Book book, Account category, Account paymentMethod) {
        if (!category.getBookType().equals(book.getBookType())) {
            throw new InvalidTransactionException(
                    String.format("장부 타입(%s)과 카테고리 타입(%s)이 일치하지 않습니다.",
                            book.getBookType(), category.getBookType()));
        }

        if (!paymentMethod.getBookType().equals(book.getBookType())) {
            throw new InvalidTransactionException(
                    String.format("장부 타입(%s)과 결제수단 타입(%s)이 일치하지 않습니다.",
                            book.getBookType(), paymentMethod.getBookType()));
        }
    }

    private void createDetail(
            JournalEntry journalEntry,
            Account account,
            DetailType detailType,
            BigDecimal amount) {

        TransactionDetail detail = TransactionDetail.builder()
                .journalEntry(journalEntry)
                .account(account)
                .detailType(detailType)
                .debitAmount(detailType == DetailType.DEBIT ? amount : BigDecimal.ZERO)
                .creditAmount(detailType == DetailType.CREDIT ? amount : BigDecimal.ZERO)
                .build();

        transactionDetailRepository.save(detail);
    }

    private String generateDescription(TransactionType type, String categoryName, BigDecimal amount) {
        return String.format("%s - %s %s원",
                type == TransactionType.INCOME ? "수입" : "지출",
                categoryName,
                amount.toString());
    }

    private void validateBookAccess(Long userId, Long bookId) {
        Book book = bookRepository.findByIdAndIsActive(bookId, true)
                .orElseThrow(() -> new BookNotFoundException(bookId));

        if (!book.getUser().getId().equals(userId)) {
            throw new UnauthorizedBookAccessException(bookId);
        }
    }
}