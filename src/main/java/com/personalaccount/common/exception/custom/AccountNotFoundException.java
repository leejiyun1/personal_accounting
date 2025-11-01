package com.personalaccount.common.exception.custom;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException() {
        super("계정과목을 찾을 수 없습니다.");
    }

    public AccountNotFoundException(String message) {
        super(message);
    }

    public AccountNotFoundException(Long id) {
        super("계정과목을 찾을 수 없습니다. ID: " + id);
    }
}