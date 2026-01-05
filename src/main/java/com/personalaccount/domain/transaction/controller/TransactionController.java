package com.personalaccount.domain.transaction.controller;

import com.personalaccount.common.dto.CommonResponse;
import com.personalaccount.common.dto.ResponseFactory;
import com.personalaccount.domain.transaction.dto.request.TransactionCreateRequest;
import com.personalaccount.domain.transaction.dto.request.TransactionUpdateRequest;
import com.personalaccount.domain.transaction.dto.response.TransactionDetailResponse;
import com.personalaccount.domain.transaction.dto.response.TransactionResponse;
import com.personalaccount.domain.transaction.entity.TransactionType;
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

    @PostMapping
    public ResponseEntity<CommonResponse<TransactionResponse>> createTransaction(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody TransactionCreateRequest request) {
        log.info("POST /api/v1/transactions - userId={}", userId);
        TransactionResponse response = transactionService.createTransaction(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseFactory.success(response, "거래 생성 완료"));
    }

    @GetMapping
    public ResponseEntity<CommonResponse<List<TransactionResponse>>> getTransactions(
            @AuthenticationPrincipal Long userId,
            @RequestParam Long bookId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("GET /api/v1/transactions - userId={}, bookId={}", userId, bookId);
        List<TransactionResponse> response = transactionService.getTransactions(
                userId, bookId, type, startDate, endDate);
        return ResponseEntity.ok(ResponseFactory.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<TransactionResponse>> getTransaction(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        log.info("GET /api/v1/transactions/{} - userId={}", id, userId);
        TransactionResponse response = transactionService.getTransaction(userId, id);
        return ResponseEntity.ok(ResponseFactory.success(response));
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<CommonResponse<TransactionDetailResponse>> getTransactionDetails(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        log.info("GET /api/v1/transactions/{}/details - userId={}", id, userId);
        TransactionDetailResponse response = transactionService.getTransactionWithDetails(userId, id);
        return ResponseEntity.ok(ResponseFactory.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<TransactionResponse>> updateTransaction(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @Valid @RequestBody TransactionUpdateRequest request) {
        log.info("PUT /api/v1/transactions/{} - userId={}", id, userId);
        TransactionResponse response = transactionService.updateTransaction(userId, id, request);
        return ResponseEntity.ok(ResponseFactory.success(response, "거래 수정 완료"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteTransaction(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        log.info("DELETE /api/v1/transactions/{} - userId={}", id, userId);
        transactionService.deleteTransaction(userId, id);
        return ResponseEntity.ok(ResponseFactory.successWithMessage("거래 삭제 완료"));
    }
}