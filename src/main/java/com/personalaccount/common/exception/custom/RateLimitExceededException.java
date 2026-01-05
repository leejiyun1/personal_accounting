package com.personalaccount.common.exception.custom;

import com.personalaccount.common.exception.BusinessException;
import com.personalaccount.common.exception.ErrorCode;

public class RateLimitExceededException extends BusinessException {

    public RateLimitExceededException() {
        super(ErrorCode.RATE_LIMIT_EXCEEDED);
    }

    public RateLimitExceededException(String message) {
        super(ErrorCode.RATE_LIMIT_EXCEEDED, message);
    }
}