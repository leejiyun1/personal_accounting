package com.personalaccount.common.exception.custom;

import com.personalaccount.common.exception.BusinessException;
import com.personalaccount.common.exception.ErrorCode;
import com.personalaccount.domain.book.entity.BookType;

public class DuplicateBookTypeException extends BusinessException {

    public DuplicateBookTypeException() {
        super(ErrorCode.DUPLICATE_BOOK_TYPE);
    }

    public DuplicateBookTypeException(String message) {
        super(ErrorCode.DUPLICATE_BOOK_TYPE, message);
    }

    public DuplicateBookTypeException(BookType bookType) {
        super(ErrorCode.DUPLICATE_BOOK_TYPE,
                String.format("%s 장부", bookType == BookType.PERSONAL ? "개인" : "사업"));
    }
}