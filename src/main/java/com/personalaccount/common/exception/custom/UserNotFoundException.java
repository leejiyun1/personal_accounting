package com.personalaccount.common.exception.custom;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {
        super("사용자를 찾을 수 없습니다.");
    }

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(Long id) {
        super("사용자를 찾을 수 없습니다. ID: " + id);
    }
}