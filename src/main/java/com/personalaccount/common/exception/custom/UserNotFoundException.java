package com.personalaccount.common.exception.custom;

import com.personalaccount.common.exception.BusinessException;
import com.personalaccount.common.exception.ErrorCode;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }

    public UserNotFoundException(String message) {
        super(ErrorCode.USER_NOT_FOUND, message);
    }

    public UserNotFoundException(Long id) {
        super(ErrorCode.USER_NOT_FOUND, "ID: " + id);
    }
}