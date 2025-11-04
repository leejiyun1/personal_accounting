package com.personalaccount.common.exception.custom;

public class AiServiceException extends RuntimeException {

    public AiServiceException() {
        super("AI 서비스 호출에 실패했습니다.");
    }

    public AiServiceException(String message) {
        super(message);
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}