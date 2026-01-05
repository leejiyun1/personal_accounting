package com.personalaccount.common.exception.custom;

import com.personalaccount.common.exception.BusinessException;
import com.personalaccount.common.exception.ErrorCode;

public class SessionNotFoundException extends BusinessException {

    public SessionNotFoundException() {
        super(ErrorCode.SESSION_NOT_FOUND);
    }

    public SessionNotFoundException(String message) {
        super(ErrorCode.SESSION_NOT_FOUND, message);
    }
}