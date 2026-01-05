package com.personalaccount.common.exception.custom;

import com.personalaccount.common.exception.BusinessException;
import com.personalaccount.common.exception.ErrorCode;

public class AiServiceException extends BusinessException {

    public AiServiceException() {
        super(ErrorCode.AI_SERVICE_ERROR);
    }

    public AiServiceException(String message) {
        super(ErrorCode.AI_SERVICE_ERROR, message);
    }

    public AiServiceException(String message, Throwable cause) {
        super(ErrorCode.AI_SERVICE_ERROR, message, cause);
    }
}