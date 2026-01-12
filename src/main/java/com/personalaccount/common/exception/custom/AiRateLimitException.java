package com.personalaccount.common.exception.custom;

import com.personalaccount.common.exception.BusinessException;
import com.personalaccount.common.exception.ErrorCode;

public class AiRateLimitException extends BusinessException {

    public AiRateLimitException() {
        super(ErrorCode.AI_RATE_LIMIT);
    }

    public AiRateLimitException(String message) {
        super(ErrorCode.AI_RATE_LIMIT, message);
    }
}