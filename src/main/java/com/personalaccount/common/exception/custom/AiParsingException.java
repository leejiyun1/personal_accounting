package com.personalaccount.common.exception.custom;

import com.personalaccount.common.exception.BusinessException;
import com.personalaccount.common.exception.ErrorCode;

public class AiParsingException extends BusinessException {

    public AiParsingException() {
        super(ErrorCode.AI_PARSING_ERROR);
    }

    public AiParsingException(String message) {
        super(ErrorCode.AI_PARSING_ERROR, message);
    }

    public AiParsingException(String message, Throwable cause) {
        super(ErrorCode.AI_PARSING_ERROR, message, cause);
    }
}