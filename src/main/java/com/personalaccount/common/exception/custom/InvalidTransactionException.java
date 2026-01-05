package com.personalaccount.common.exception.custom;

import com.personalaccount.common.exception.BusinessException;
import com.personalaccount.common.exception.ErrorCode;

public class InvalidTransactionException extends BusinessException {

    public InvalidTransactionException() {
        super(ErrorCode.INVALID_TRANSACTION);
    }

    public InvalidTransactionException(String message) {
        super(ErrorCode.INVALID_TRANSACTION, message);
    }
}