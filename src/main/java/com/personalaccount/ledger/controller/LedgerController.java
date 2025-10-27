package com.personalaccount.ledger.controller;

import com.personalaccount.common.dto.CommonResponse;
import com.personalaccount.common.dto.ResponseFactory;
import com.personalaccount.ledger.dto.response.AccountLedgerResponse;
import com.personalaccount.ledger.dto.response.LedgerSummaryResponse;
import com.personalaccount.ledger.service.LedgerService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    /**
     * 재무 요약 조회 (손익계산서 + 재무상태표)
     */
    @GetMapping("/summary")
    public ResponseEntity<CommonResponse<LedgerSummaryResponse>> getLedgerSummary(
            @AuthenticationPrincipal Long userId,
            @RequestParam @NotNull Long bookId,
            @RequestParam @NotNull @Pattern(regexp = "\\d{4}-\\d{2}") String yearMonth
    ) {
        log.info("재무 요약 API 호출: userId={}, bookId={}, yearMonth={}", userId, bookId, yearMonth);

        LedgerSummaryResponse response = ledgerService.getLedgerSummary(userId, bookId, yearMonth);

        return ResponseEntity.ok(
                ResponseFactory.success(response)
        );
    }

    /**
     * 계정별 원장 조회
     */
    @GetMapping("/accounts")
    public ResponseEntity<CommonResponse<AccountLedgerResponse>> getAccountLedger(
            @AuthenticationPrincipal Long userId,
            @RequestParam @NotNull Long bookId,
            @RequestParam @NotNull Long accountId,
            @RequestParam @NotNull @Pattern(regexp = "\\d{4}-\\d{2}") String yearMonth
    ) {
        log.info("계정별 원장 API 호출: userId={}, bookId={}, accountId={}, yearMonth={}",
                userId, bookId, accountId, yearMonth);

        AccountLedgerResponse response = ledgerService.getAccountLedger(userId, bookId, accountId, yearMonth);

        return ResponseEntity.ok(
                ResponseFactory.success(response)
        );
    }
}