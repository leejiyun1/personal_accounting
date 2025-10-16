package com.personalaccount.transaction.controller;

import com.personalaccount.common.dto.CommonResponse;
import com.personalaccount.common.dto.ResponseFactory;
import com.personalaccount.transaction.dto.mapper.TransactionMapper;
import com.personalaccount.transaction.dto.request.TransactionCreateRequest;
import com.personalaccount.transaction.dto.request.TransactionUpdateRequest;
import com.personalaccount.transaction.dto.response.TransactionResponse;
import com.personalaccount.transaction.entity.Transaction;
import com.personalaccount.transaction.entity.TransactionType;
import com.personalaccount.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<CommonResponse<TransactionResponse>> createTransaction(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody TransactionCreateRequest request
    ) {
        log.info("거래 생성 API 호출: userId={}, bookId={}", userId, request.getBookId());

        Transaction transaction = transactionService.createTransaction(userId, request);
        TransactionResponse response = TransactionMapper.toResponse(transaction);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseFactory.success(response, "거래 생성 완료"));
    }

    @GetMapping
    public ResponseEntity<CommonResponse<List<TransactionResponse>>> getTransactions(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam Long bookId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("거래 목록 조회 API 호출: userId={}, bookId={}, type={}, start={}, end={}",
                userId, bookId, type, startDate, endDate);

        List<Transaction> transactions;

        if (startDate != null && endDate != null) {
            transactions = transactionService.getTransactionsByDateRange(userId, bookId, startDate, endDate);
        } else if (type != null) {
            transactions = transactionService.getTransactionsByType(userId, bookId, type);
        } else {
            transactions = transactionService.getTransactions(userId, bookId);
        }

        List<TransactionResponse> response = transactions.stream()
                .map(TransactionMapper::toResponse)
                .toList();

        return ResponseEntity.ok(ResponseFactory.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<TransactionResponse>> getTransaction(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id
    ) {
        log.info("거래 상세 조회 API 호출: userId={}, transactionId={}", userId, id);

        Transaction transaction = transactionService.getTransaction(userId, id);
        TransactionResponse response = TransactionMapper.toResponse(transaction);

        return ResponseEntity.ok(ResponseFactory.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<TransactionResponse>> updateTransaction(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody TransactionUpdateRequest request
    ) {
        log.info("거래 수정 API 호출: userId={}, transactionId={}", userId, id);

        Transaction transaction = transactionService.updateTransaction(userId, id, request);
        TransactionResponse response = TransactionMapper.toResponse(transaction);

        return ResponseEntity.ok(ResponseFactory.success(response, "거래 수정 완료"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteTransaction(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id
    ) {
        log.info("거래 삭제 API 호출: userId={}, transactionId={}", userId, id);

        transactionService.deleteTransaction(userId, id);

        return ResponseEntity.ok(ResponseFactory.successWithMessage("거래 삭제 완료"));
    }
}