package com.personalaccount.common.exception.custom;

import com.personalaccount.common.exception.BusinessException;
import com.personalaccount.common.exception.ErrorCode;

public class AiBadRequestException extends BusinessException {

    public AiBadRequestException() {
        super(ErrorCode.AI_BAD_REQUEST);
    }

    public AiBadRequestException(String message) {
        super(ErrorCode.AI_BAD_REQUEST, message);
    }
}