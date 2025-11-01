package com.personalaccount.application.report.ledger.service;

import com.personalaccount.application.report.ledger.dto.response.AccountLedgerResponse;
import com.personalaccount.application.report.ledger.dto.response.LedgerSummaryResponse;

public interface LedgerService {

    /**
     * 재무 요약 조회 (손익계산서 + 재무상태표)
     * @param userId 사용자 ID
     * @param bookId 장부 ID
     * @param yearMonth 조회 월 (YYYY-MM)
     * @return 재무 요약
     */
    LedgerSummaryResponse getLedgerSummary(Long userId, Long bookId, String yearMonth);

    /**
     * 계정별 원장 조회
     * @param userId 사용자 ID
     * @param bookId 장부 ID
     * @param accountId 계정과목 ID
     * @param yearMonth 조회 월 (YYYY-MM)
     * @return 계정별 원장
     */
    AccountLedgerResponse getAccountLedger(Long userId, Long bookId, Long accountId, String yearMonth);
}
