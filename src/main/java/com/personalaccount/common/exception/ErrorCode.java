package com.personalaccount.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 에러 코드 정의

 * 코드 체계:
 * - U0xx: User (사용자)
 * - B0xx: Book (장부)
 * - T0xx: Transaction (거래)
 * - A0xx: Account (계정과목)
 * - AUTH0xx: Authentication (인증)
 * - AI0xx: AI (인공지능)
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // User (U0xx)
    USER_NOT_FOUND("U001", "사용자를 찾을 수 없습니다"),
    DUPLICATE_EMAIL("U002", "이미 사용 중인 이메일입니다"),

    // Book (B0xx)
    BOOK_NOT_FOUND("B001", "장부를 찾을 수 없습니다"),
    DUPLICATE_BOOK_TYPE("B002", "이미 같은 타입의 장부가 존재합니다"),
    UNAUTHORIZED_BOOK_ACCESS("B003", "해당 장부에 접근 권한이 없습니다"),

    // Transaction (T0xx)
    TRANSACTION_NOT_FOUND("T001", "거래를 찾을 수 없습니다"),
    INVALID_TRANSACTION("T002", "유효하지 않은 거래입니다"),

    // Account (A0xx)
    ACCOUNT_NOT_FOUND("A001", "계정과목을 찾을 수 없습니다"),

    // Authentication (AUTH0xx)
    UNAUTHORIZED("AUTH001", "인증에 실패했습니다"),
    RATE_LIMIT_EXCEEDED("AUTH002", "요청 횟수를 초과했습니다"),

    // AI (AI0xx)
    AI_SERVICE_ERROR("AI001", "AI 서비스 호출에 실패했습니다"),
    AI_PARSING_ERROR("AI002", "AI 응답을 파싱할 수 없습니다"),
    SESSION_NOT_FOUND("AI003", "세션을 찾을 수 없습니다");

    private final String code;
    private final String message;
}