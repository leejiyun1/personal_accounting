package com.personalaccount.common.exception;

import lombok.Getter;

/**
 * 비즈니스 로직 예외의 기본 클래스

 * 모든 커스텀 예외는 이 클래스를 상속받아 ErrorCode를 사용
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String detail;

    /**
     * ErrorCode만 사용하는 생성자
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = null;
    }

    /**
     * ErrorCode + 상세 정보를 함께 사용하는 생성자
     *
     * @param errorCode 에러 코드
     * @param detail 상세 정보 (예: "ID: 1")
     */
    public BusinessException(ErrorCode errorCode, String detail) {
        super(errorCode.getMessage() + (detail != null ? ": " + detail : ""));
        this.errorCode = errorCode;
        this.detail = detail;
    }

    /**
     * ErrorCode + Cause를 함께 사용하는 생성자
     *
     * @param errorCode 에러 코드
     * @param cause 원인 예외
     */
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.detail = null;
    }

    /**
     * 모든 정보를 포함하는 생성자
     */
    public BusinessException(ErrorCode errorCode, String detail, Throwable cause) {
        super(errorCode.getMessage() + (detail != null ? ": " + detail : ""), cause);
        this.errorCode = errorCode;
        this.detail = detail;
    }
}