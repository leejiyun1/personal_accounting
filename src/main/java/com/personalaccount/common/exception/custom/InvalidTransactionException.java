package com.personalaccount.common.exception.custom;

public class InvalidTransactionException extends RuntimeException {

    public InvalidTransactionException() {
        super("유효하지 않은 거래입니다.");
    }

    public InvalidTransactionException(String message) {
        super(message);
    }
}