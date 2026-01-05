package com.personalaccount.common.exception.handler;

import com.personalaccount.common.dto.CommonResponse;
import com.personalaccount.common.dto.ResponseFactory;
import com.personalaccount.common.exception.custom.*;
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
    public ResponseEntity<CommonResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex
    ) {
        log.warn("Validation failed: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.<Map<String, String>>builder()
                        .success(false)
                        .data(errors)
                        .message("입력값 검증 실패")
                        .build());
    }

    // === Auth 예외 ===

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<CommonResponse<Void>> handleUnauthorized(
            UnauthorizedException ex
    ) {
        log.warn("UnauthorizedException: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)  // 401
                .body(ResponseFactory.error(ex.getMessage()));
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<CommonResponse<Void>> handleRateLimitExceeded(
            RateLimitExceededException ex
    ) {
        log.warn("RateLimitExceededException: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)  // 429
                .body(ResponseFactory.error(ex.getMessage()));
    }

    // === User 예외 ===

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<CommonResponse<Void>> handleUserNotFound(
            UserNotFoundException ex
    ) {
        log.warn("UserNotFoundException: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)  // 404
                .body(ResponseFactory.error(ex.getMessage()));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<CommonResponse<Void>> handleDuplicateEmail(
            DuplicateEmailException ex
    ) {
        log.warn("DuplicateEmailException: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)  // 400
                .body(ResponseFactory.error(ex.getMessage()));
    }

    // === Book 예외===

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<CommonResponse<Void>> handleBookNotFound(
            BookNotFoundException ex
    ) {
        log.warn("BookNotFoundException: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ResponseFactory.error(ex.getMessage()));
    }

    @ExceptionHandler(DuplicateBookTypeException.class)
    public ResponseEntity<CommonResponse<Void>> handleDuplicateBookType(
            DuplicateBookTypeException ex
    ) {
        log.warn("DuplicateBookTypeException: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResponseFactory.error(ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedBookAccessException.class)
    public ResponseEntity<CommonResponse<Void>> handleUnauthorizedBookAccess(
            UnauthorizedBookAccessException ex
    ) {
        log.warn("UnauthorizedBookAccessException: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ResponseFactory.error(ex.getMessage()));
    }

    // === Account 예외 ===

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<CommonResponse<Void>> handleAccountNotFound(
            AccountNotFoundException ex
    ) {
        log.warn("AccountNotFoundException: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ResponseFactory.error(ex.getMessage()));
    }

    // === Transaction 예외 ===

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<CommonResponse<Void>> handleTransactionNotFound(
            TransactionNotFoundException ex
    ) {
        log.warn("TransactionNotFoundException: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ResponseFactory.error(ex.getMessage()));
    }

    @ExceptionHandler(InvalidTransactionException.class)
    public ResponseEntity<CommonResponse<Void>> handleInvalidTransaction(
            InvalidTransactionException ex
    ) {
        log.warn("InvalidTransactionException: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResponseFactory.error(ex.getMessage()));
    }

    // === AI 예외 ===

    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<CommonResponse<Void>> handleAiServiceException(
            AiServiceException ex
    ) {
        log.error("AiServiceException: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)  // 503
                .body(ResponseFactory.error("AI 서비스가 일시적으로 사용 불가능합니다. 잠시 후 다시 시도해주세요."));
    }

    @ExceptionHandler(SessionNotFoundException.class)
    public ResponseEntity<CommonResponse<Void>> handleSessionNotFound(
            SessionNotFoundException ex
    ) {
        log.warn("SessionNotFoundException: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)  // 400
                .body(ResponseFactory.error("대화 세션이 만료되었습니다. 새로운 대화를 시작해주세요."));
    }

    @ExceptionHandler(AiParsingException.class)
    public ResponseEntity<CommonResponse<Void>> handleAiParsingException(
            AiParsingException ex
    ) {
        log.error("AiParsingException: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)  // 500
                .body(ResponseFactory.error("AI 응답 처리 중 오류가 발생했습니다. 다시 시도해주세요."));
    }

    // === 모든 예외 (환경별 분기) ===

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleException(
            Exception ex
    ) {
        log.error("Unexpected error: ", ex);

        // 운영 환경에서는 상세 에러 숨김
        String message = isProdEnvironment()
                ? "서버 오류가 발생했습니다."
                : "서버 오류: " + ex.getMessage();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)  // 500
                .body(ResponseFactory.error(message));
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