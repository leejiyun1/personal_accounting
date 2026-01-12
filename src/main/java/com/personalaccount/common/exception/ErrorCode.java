package com.personalaccount.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    USER_NOT_FOUND("U001", "사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    DUPLICATE_EMAIL("U002", "이미 사용 중인 이메일입니다", HttpStatus.BAD_REQUEST),

    BOOK_NOT_FOUND("B001", "장부를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    DUPLICATE_BOOK_TYPE("B002", "이미 같은 타입의 장부가 존재합니다", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_BOOK_ACCESS("B003", "해당 장부에 접근 권한이 없습니다", HttpStatus.FORBIDDEN),

    TRANSACTION_NOT_FOUND("T001", "거래를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    INVALID_TRANSACTION("T002", "유효하지 않은 거래입니다", HttpStatus.BAD_REQUEST),

    ACCOUNT_NOT_FOUND("A001", "계정과목을 찾을 수 없습니다", HttpStatus.NOT_FOUND),

    UNAUTHORIZED("AUTH001", "인증에 실패했습니다", HttpStatus.UNAUTHORIZED),
    RATE_LIMIT_EXCEEDED("AUTH002", "요청 횟수를 초과했습니다", HttpStatus.TOO_MANY_REQUESTS),

    AI_SERVICE_ERROR("AI001", "AI 서비스 호출에 실패했습니다", HttpStatus.SERVICE_UNAVAILABLE),
    AI_PARSING_ERROR("AI002", "AI 응답을 파싱할 수 없습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    SESSION_NOT_FOUND("AI003", "세션을 찾을 수 없습니다", HttpStatus.BAD_REQUEST),
    AI_RATE_LIMIT("AI004", "AI API 할당량을 초과했습니다", HttpStatus.TOO_MANY_REQUESTS),
    AI_BAD_REQUEST("AI005", "AI 요청이 올바르지 않습니다", HttpStatus.BAD_REQUEST),
    AI_TIMEOUT("AI006", "AI 응답 시간이 초과되었습니다", HttpStatus.GATEWAY_TIMEOUT);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}