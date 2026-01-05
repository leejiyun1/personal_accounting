package com.personalaccount.common.exception.custom;

import com.personalaccount.common.exception.BusinessException;
import com.personalaccount.common.exception.ErrorCode;

public class UnauthorizedBookAccessException extends BusinessException {

    public UnauthorizedBookAccessException() {
        super(ErrorCode.UNAUTHORIZED_BOOK_ACCESS);
    }

    public UnauthorizedBookAccessException(String message) {
        super(ErrorCode.UNAUTHORIZED_BOOK_ACCESS, message);
    }

    public UnauthorizedBookAccessException(Long bookId) {
        super(ErrorCode.UNAUTHORIZED_BOOK_ACCESS, "장부 ID: " + bookId);
    }
}