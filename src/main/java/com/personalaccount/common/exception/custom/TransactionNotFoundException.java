package com.personalaccount.common.exception.custom;

public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(Long id) {
        super("거래를 찾을 수 없습니다. ID: " + id);
    }

    public TransactionNotFoundException(String message) {
        super(message);
    }
}