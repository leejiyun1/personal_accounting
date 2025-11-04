package com.personalaccount.common.exception.custom;

public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException() {
        super("요청 횟수를 초과했습니다. 잠시 후 다시 시도해주세요.");
    }

    public RateLimitExceededException(String message) {
        super(message);
    }
}