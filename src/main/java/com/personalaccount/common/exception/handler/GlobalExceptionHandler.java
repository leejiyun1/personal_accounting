package com.personalaccount.common.exception.handler;

import com.personalaccount.common.dto.CommonResponse;
import com.personalaccount.common.dto.ResponseFactory;
import com.personalaccount.common.exception.custom.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

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
    public ResponseEntity<CommonResponse<Void>> handleInvlidTransaction(
            InvalidTransactionException ex
    ) {
        log.warn("InvalidTransactionException: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResponseFactory.error(ex.getMessage()));
    }

    // === 모든 예외 ===

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleException(
            Exception ex
    ) {
        log.error("Unexpected error: ", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)  // 500
                .body(ResponseFactory.error("서버 오류가 발생했습니다."));
    }
}