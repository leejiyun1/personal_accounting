package com.personalaccount.common.exception.custom;

public class SessionNotFoundException extends RuntimeException {

    public SessionNotFoundException() {
        super("세션을 찾을 수 없습니다.");
    }

    public SessionNotFoundException(String message) {
        super(message);
    }
}