package com.personalaccount.common.exception.custom;

import com.personalaccount.common.exception.BusinessException;
import com.personalaccount.common.exception.ErrorCode;

public class BookNotFoundException extends BusinessException {

    public BookNotFoundException() {
        super(ErrorCode.BOOK_NOT_FOUND);
    }

    public BookNotFoundException(String message) {
        super(ErrorCode.BOOK_NOT_FOUND, message);
    }

    public BookNotFoundException(Long id) {
        super(ErrorCode.BOOK_NOT_FOUND, "ID: " + id);
    }
}