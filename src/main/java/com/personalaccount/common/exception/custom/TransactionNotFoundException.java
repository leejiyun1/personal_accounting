package com.personalaccount.common.exception.custom;

import com.personalaccount.common.exception.BusinessException;
import com.personalaccount.common.exception.ErrorCode;

public class TransactionNotFoundException extends BusinessException {

    public TransactionNotFoundException() {
        super(ErrorCode.TRANSACTION_NOT_FOUND);
    }

    public TransactionNotFoundException(String message) {
        super(ErrorCode.TRANSACTION_NOT_FOUND, message);
    }

    public TransactionNotFoundException(Long id) {
        super(ErrorCode.TRANSACTION_NOT_FOUND, "ID: " + id);
    }
}