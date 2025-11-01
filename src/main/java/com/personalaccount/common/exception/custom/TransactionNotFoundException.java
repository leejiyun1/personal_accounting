package com.personalaccount.common.exception.custom;

public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException() {
        super("거래를 찾을 수 없습니다.");
    }

    public TransactionNotFoundException(String message) {
        super(message);
    }

    public TransactionNotFoundException(Long id) {
        super("거래를 찾을 수 없습니다. ID: " + id);
    }
}