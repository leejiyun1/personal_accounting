package com.personalaccount.common.exception.custom;

public class AiParsingException extends RuntimeException {

    public AiParsingException() {
        super("AI 응답을 파싱할 수 없습니다.");
    }

    public AiParsingException(String message) {
        super(message);
    }

    public AiParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}