package com.personalaccount.common.exception.custom;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(Long id) {
        super("장부를 찾을 수 없습니다. ID: " +  id);
    }

    public BookNotFoundException(String message) {
        super(message);
    }
}
