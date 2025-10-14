package com.personalaccount.common.exception.custom;

import com.personalaccount.book.entity.BookType;

public class DuplicateBookTypeException extends RuntimeException {

    public DuplicateBookTypeException(BookType bookType) {
        super(String.format(
                "이미 %s 장부가 존재합니다. 같은 타입의 장부는 1개만 생성할 수 있습니다.",
                bookType == BookType.PERSONAL ? "개인" : "사업"
        ));
    }
}
