package com.personalaccount.common.exception.custom;

public class UnauthorizedBookAccessException extends RuntimeException {

    public UnauthorizedBookAccessException(String message) {
        super(message);
    }

    public UnauthorizedBookAccessException(Long bookId) {
        super(String.format("장부(Id: %d)에 접근할 권한이 없습니다.", bookId));
    }
}
