package com.personalaccount.domain.transaction.service.impl;

import com.personalaccount.domain.account.entity.Account;
import com.personalaccount.domain.account.entity.AccountType;
import com.personalaccount.domain.account.repository.AccountRepository;
import com.personalaccount.domain.book.entity.Book;
import com.personalaccount.domain.book.repository.BookRepository;
import com.personalaccount.common.exception.custom.*;
import com.personalaccount.domain.transaction.dto.mapper.TransactionMapper;
import com.personalaccount.domain.transaction.dto.request.TransactionCreateRequest;
import com.personalaccount.domain.transaction.dto.request.TransactionSearchCondition;
import com.personalaccount.domain.transaction.dto.request.TransactionUpdateRequest;
import com.personalaccount.domain.transaction.dto.response.TransactionDetailResponse;
import com.personalaccount.domain.transaction.dto.response.TransactionResponse;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public TransactionResponse createTransaction(Long userId, TransactionCreateRequest request) {
        log.info("거래 생성 요청: userId={}, bookId={}, type={}, amount={}",
                userId, request.getBookId(), request.getType(), request.getAmount());

        Book book = validateAndGetBook(userId, request.getBookId());

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
                .amount(request.getAmount())
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

        return TransactionMapper.toResponse(savedTransaction);
    }

    @Override
    public List<TransactionResponse> getTransactions(
            Long userId,
            Long bookId,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate) {

        log.debug("거래 목록 조회: userId={}, bookId={}, type={}, start={}, end={}",
                userId, bookId, type, startDate, endDate);

        validateAndGetBook(userId, bookId);

        TransactionSearchCondition condition = TransactionSearchCondition.builder()
                .bookId(bookId)
                .type(type)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        List<Transaction> transactions = transactionRepository.searchTransactions(condition);

        return transactions.stream()
                .map(TransactionMapper::toResponse)
                .toList();
    }

    @Override
    public TransactionResponse getTransaction(Long userId, Long id) {
        log.debug("거래 상세 조회: userId={}, transactionId={}", userId, id);

        Transaction transaction = getTransactionEntity(userId, id);

        return TransactionMapper.toResponse(transaction);
    }

    @Override
    public TransactionDetailResponse getTransactionWithDetails(Long userId, Long id) {
        log.debug("거래 상세 조회(복식부기 포함): userId={}, transactionId={}", userId, id);

        Transaction transaction = getTransactionEntity(userId, id);
        List<JournalEntry> journalEntries = journalEntryRepository.findByTransactionId(id);

        // N+1 해결: IN 절로 한 번에 조회
        List<Long> journalEntryIds = journalEntries.stream()
                .map(JournalEntry::getId)
                .toList();

        List<TransactionDetail> allDetails = transactionDetailRepository
                .findByJournalEntryIdIn(journalEntryIds);

        // JournalEntry별로 그룹화
        Map<Long, List<TransactionDetail>> detailsMap = allDetails.stream()
                .collect(Collectors.groupingBy(
                        detail -> detail.getJournalEntry().getId()
                ));

        // 순서 유지하면서 List<List<TransactionDetail>> 생성
        List<List<TransactionDetail>> detailsList = journalEntries.stream()
                .map(je -> detailsMap.getOrDefault(je.getId(), List.of()))
                .toList();

        return TransactionMapper.toDetailResponse(transaction, journalEntries, detailsList);
    }

    @Transactional
    @Override
    public TransactionResponse updateTransaction(Long userId, Long id, TransactionUpdateRequest request) {
        log.info("거래 수정 요청: userId={}, transactionId={}", userId, id);

        Transaction transaction = getTransactionEntity(userId, id);

        if (request.getMemo() != null) {
            transaction.updateMemo(request.getMemo());
        }

        log.info("거래 수정 완료: transactionId={}", id);

        return TransactionMapper.toResponse(transaction);
    }

    @Transactional
    @Override
    public void deleteTransaction(Long userId, Long id) {
        log.info("거래 삭제 요청: userId={}, transactionId={}", userId, id);

        Transaction transaction = getTransactionEntity(userId, id);
        transaction.deactivate();

        log.info("거래 삭제 완료: transactionId={}", id);
    }

    // === Private Helper Methods ===

    private Transaction getTransactionEntity(Long userId, Long id) {
        Transaction transaction = transactionRepository.findByIdWithBookAndUser(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        if (!transaction.getBook().getUser().getId().equals(userId)) {
            throw new UnauthorizedBookAccessException(transaction.getBook().getId());
        }

        return transaction;
    }

    private void createDoubleEntryDetails(
            JournalEntry journalEntry,
            TransactionType type,
            Account category,
            Account paymentMethod,
            BigDecimal amount) {

        List<TransactionDetail> details = new ArrayList<>();

        if (type == TransactionType.INCOME) {
            details.add(createDetailObject(journalEntry, paymentMethod, DetailType.DEBIT, amount));
            details.add(createDetailObject(journalEntry, category, DetailType.CREDIT, amount));
        } else {
            details.add(createDetailObject(journalEntry, category, DetailType.DEBIT, amount));
            details.add(createDetailObject(journalEntry, paymentMethod, DetailType.CREDIT, amount));
        }

        validateDoubleEntry(details);
        transactionDetailRepository.saveAll(details);
    }

    private void validateDoubleEntry(List<TransactionDetail> details) {
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

        AccountType expectedType = (transactionType == TransactionType.INCOME)
                ? AccountType.REVENUE
                : AccountType.EXPENSE;

        if (category.getAccountType() != expectedType) {
            throw new InvalidTransactionException(
                    String.format("%s 거래는 %s 계정과목을 사용해야 합니다.",
                            transactionType == TransactionType.INCOME ? "수입" : "지출",
                            expectedType == AccountType.REVENUE ? "수익" : "비용"));
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

    private TransactionDetail createDetailObject(
            JournalEntry journalEntry,
            Account account,
            DetailType detailType,
            BigDecimal amount) {

        return TransactionDetail.builder()
                .journalEntry(journalEntry)
                .account(account)
                .detailType(detailType)
                .debitAmount(detailType == DetailType.DEBIT ? amount : BigDecimal.ZERO)
                .creditAmount(detailType == DetailType.CREDIT ? amount : BigDecimal.ZERO)
                .build();
    }

    private String generateDescription(TransactionType type, String categoryName, BigDecimal amount) {
        return String.format("%s - %s %s원",
                type == TransactionType.INCOME ? "수입" : "지출",
                categoryName,
                amount.toString());
    }

    private Book validateAndGetBook(Long userId, Long bookId) {
        Book book = bookRepository.findByIdAndIsActive(bookId, true)
                .orElseThrow(() -> new BookNotFoundException(bookId));

        if (!book.getUser().getId().equals(userId)) {
            throw new UnauthorizedBookAccessException(bookId);
        }

        return book;
    }
}