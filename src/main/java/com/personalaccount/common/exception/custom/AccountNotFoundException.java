package com.personalaccount.common.exception.custom;

import com.personalaccount.common.exception.BusinessException;
import com.personalaccount.common.exception.ErrorCode;

public class AccountNotFoundException extends BusinessException {

    public AccountNotFoundException() {
        super(ErrorCode.ACCOUNT_NOT_FOUND);
    }

    public AccountNotFoundException(String message) {
        super(ErrorCode.ACCOUNT_NOT_FOUND, message);
    }

    public AccountNotFoundException(Long id) {
        super(ErrorCode.ACCOUNT_NOT_FOUND, "ID: " + id);
    }
}