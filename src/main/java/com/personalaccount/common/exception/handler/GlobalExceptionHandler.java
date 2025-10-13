package com.personalaccount.common.exception.handler;

import com.personalaccount.common.dto.CommonResponse;
import com.personalaccount.common.dto.ResponseFactory;
import com.personalaccount.common.exception.custom.DuplicateEmailException;
import com.personalaccount.common.exception.custom.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리기
 *
 * @RestControllerAdvice:
 * - 모든 Controller에서 발생하는 예외를 한 곳에서 처리
 * - @ControllerAdvice + @ResponseBody
 *
 * 역할:
 * - Controller에서 예외 발생 시 자동으로 잡아서 처리
 * - 일관된 에러 응답 반환
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * UserNotFoundException 처리
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<CommonResponse<Void>> handleUserNotFound(
            UserNotFoundException ex
    ) {
        log.warn("UserNotFoundException: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)  // 404
                .body(ResponseFactory.error(ex.getMessage()));
    }

    /**
     * DuplicateEmailException 처리
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<CommonResponse<Void>> handleDuplicateEmail(
            DuplicateEmailException ex
    ) {
        log.warn("DuplicateEmailException: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)  // 400
                .body(ResponseFactory.error(ex.getMessage()));
    }

    /**
     * 모든 예외 처리 (catch-all)
     */
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