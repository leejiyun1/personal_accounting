// src/main/java/com/personalaccount/common/exception/handler/GlobalExceptionHandler.java
package com.personalaccount.common.exception.handler;

import com.personalaccount.common.dto.ErrorResponse;
import com.personalaccount.common.exception.BusinessException;
import com.personalaccount.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Environment environment;

    // === Validation 예외 ===

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex
    ) {
        log.warn("Validation failed: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", "VALIDATION_ERROR");
        response.put("message", "입력값 검증 실패");
        response.put("errors", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // === Business 예외 (통합 처리) ===

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex
    ) {
        ErrorCode errorCode = ex.getErrorCode();

        log.warn("BusinessException: code={}, message={}",
                errorCode.getCode(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .errorCode(errorCode.getCode())
                .message(ex.getMessage())
                .build();

        HttpStatus status = determineHttpStatus(errorCode);

        return ResponseEntity
                .status(status)
                .body(response);
    }

    /**
     * ErrorCode에 따라 HTTP 상태 코드 결정
     */
    private HttpStatus determineHttpStatus(ErrorCode errorCode) {
        String code = errorCode.getCode();

        // UNAUTHORIZED (401)
        if (code.startsWith("AUTH")) {
            if (code.equals("AUTH002")) {
                return HttpStatus.TOO_MANY_REQUESTS;  // Rate Limit
            }
            return HttpStatus.UNAUTHORIZED;
        }

        // SERVICE_UNAVAILABLE (503)
        if (code.startsWith("AI")) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }

        // FORBIDDEN (403)
        if (code.equals("B003")) {  // UNAUTHORIZED_BOOK_ACCESS
            return HttpStatus.FORBIDDEN;
        }

        // NOT_FOUND (404)
        if (code.endsWith("001")) {
            return HttpStatus.NOT_FOUND;
        }

        // BAD_REQUEST (400) - 기본
        return HttpStatus.BAD_REQUEST;
    }

    // === 모든 예외 (환경별 분기) ===

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception ex
    ) {
        log.error("Unexpected error: ", ex);

        // 운영 환경에서는 상세 에러 숨김
        String message = isProdEnvironment()
                ? "서버 오류가 발생했습니다."
                : "서버 오류: " + ex.getMessage();

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .errorCode("INTERNAL_SERVER_ERROR")
                .message(message)
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    private boolean isProdEnvironment() {
        String[] profiles = environment.getActiveProfiles();
        for (String profile : profiles) {
            if ("prod".equals(profile)) {
                return true;
            }
        }
        return false;
    }
}