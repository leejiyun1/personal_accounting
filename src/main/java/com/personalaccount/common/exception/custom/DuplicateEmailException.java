package com.personalaccount.common.exception.custom;

import com.personalaccount.common.exception.BusinessException;
import com.personalaccount.common.exception.ErrorCode;

public class DuplicateEmailException extends BusinessException {

    public DuplicateEmailException() {
        super(ErrorCode.DUPLICATE_EMAIL);
    }

    public DuplicateEmailException(String message) {
        super(ErrorCode.DUPLICATE_EMAIL, message);
    }

    public DuplicateEmailException(String email, boolean withPrefix) {
        super(ErrorCode.DUPLICATE_EMAIL, email);
    }
}