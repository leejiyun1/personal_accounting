package com.personalaccount.domain.transaction.controller;

import com.personalaccount.common.dto.CommonResponse;
import com.personalaccount.common.dto.ResponseFactory;
import com.personalaccount.domain.transaction.dto.mapper.TransactionMapper;
import com.personalaccount.domain.transaction.dto.request.TransactionCreateRequest;
import com.personalaccount.domain.transaction.dto.request.TransactionUpdateRequest;
import com.personalaccount.domain.transaction.dto.response.TransactionDetailResponse;
import com.personalaccount.domain.transaction.dto.response.TransactionResponse;
import com.personalaccount.domain.transaction.entity.JournalEntry;
import com.personalaccount.domain.transaction.entity.Transaction;
import com.personalaccount.domain.transaction.entity.TransactionDetail;
import com.personalaccount.domain.transaction.entity.TransactionType;
import com.personalaccount.domain.transaction.repository.JournalEntryRepository;
import com.personalaccount.domain.transaction.repository.TransactionDetailRepository;
import com.personalaccount.domain.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final JournalEntryRepository journalEntryRepository;
    private final TransactionDetailRepository transactionDetailRepository;

    @PostMapping
    public ResponseEntity<CommonResponse<TransactionResponse>> createTransaction(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody TransactionCreateRequest request) {

        log.info("거래 생성 API 호출: userId={}, bookId={}", userId, request.getBookId());

        Transaction transaction = transactionService.createTransaction(userId, request);
        TransactionResponse response = TransactionMapper.toResponse(transaction);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseFactory.success(response, "거래 생성 완료"));
    }

    @GetMapping
    public ResponseEntity<CommonResponse<List<TransactionResponse>>> getTransactions(
            @AuthenticationPrincipal Long userId,
            @RequestParam Long bookId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("거래 목록 조회 API: userId={}, bookId={}, type={}, start={}, end={}",
                userId, bookId, type, startDate, endDate);

        List<Transaction> transactions = transactionService.getTransactions(
                userId, bookId, type, startDate, endDate);

        List<TransactionResponse> response = transactions.stream()
                .map(TransactionMapper::toResponse)
                .toList();

        return ResponseEntity.ok(ResponseFactory.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<TransactionResponse>> getTransaction(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {

        log.info("거래 상세 조회 API 호출: userId={}, transactionId={}", userId, id);

        Transaction transaction = transactionService.getTransaction(userId, id);
        TransactionResponse response = TransactionMapper.toResponse(transaction);

        return ResponseEntity.ok(ResponseFactory.success(response));
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<CommonResponse<TransactionDetailResponse>> getTransactionDetails(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {

        log.info("거래 상세 조회(복식부기 포함) API 호출: userId={}, transactionId={}", userId, id);

        Transaction transaction = transactionService.getTransaction(userId, id);
        List<JournalEntry> journalEntries = journalEntryRepository.findByTransactionId(id);
        List<List<TransactionDetail>> detailsList = journalEntries.stream()
                .map(je -> transactionDetailRepository.findByJournalEntryId(je.getId()))
                .toList();

        TransactionDetailResponse response = TransactionMapper.toDetailResponse(
                transaction, journalEntries, detailsList);

        return ResponseEntity.ok(ResponseFactory.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<TransactionResponse>> updateTransaction(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody TransactionUpdateRequest request) {

        log.info("거래 수정 API 호출: userId={}, transactionId={}", userId, id);

        Transaction transaction = transactionService.updateTransaction(userId, id, request);
        TransactionResponse response = TransactionMapper.toResponse(transaction);

        return ResponseEntity.ok(ResponseFactory.success(response, "거래 수정 완료"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteTransaction(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {

        log.info("거래 삭제 API 호출: userId={}, transactionId={}", userId, id);

        transactionService.deleteTransaction(userId, id);

        return ResponseEntity.ok(ResponseFactory.successWithMessage("거래 삭제 완료"));
    }
}