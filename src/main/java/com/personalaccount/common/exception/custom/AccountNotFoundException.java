package com.personalaccount.common.exception.custom;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(Long id) {
        super("계정과목을 찾을 수 없습니다. ID: " + id);
    }

    public AccountNotFoundException(String code) {
        super("계정과목을 찾을 수 없습니다. Code: " + code);
    }
}