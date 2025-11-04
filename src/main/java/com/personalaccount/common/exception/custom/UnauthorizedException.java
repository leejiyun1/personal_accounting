package com.personalaccount.common.exception.custom;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("인증에 실패했습니다.");
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}