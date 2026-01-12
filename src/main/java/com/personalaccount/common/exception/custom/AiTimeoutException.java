package com.personalaccount.common.exception.custom;

import com.personalaccount.common.exception.BusinessException;
import com.personalaccount.common.exception.ErrorCode;

public class AiTimeoutException extends BusinessException {

    public AiTimeoutException() {
        super(ErrorCode.AI_TIMEOUT);
    }

    public AiTimeoutException(String message) {
        super(ErrorCode.AI_TIMEOUT, message);
    }
}