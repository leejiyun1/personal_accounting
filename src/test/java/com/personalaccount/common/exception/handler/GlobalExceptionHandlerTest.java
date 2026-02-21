package com.personalaccount.common.exception.handler;

import com.personalaccount.common.dto.ErrorResponse;
import com.personalaccount.common.exception.custom.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler 테스트")
class GlobalExceptionHandlerTest {

    @Mock
    private Environment environment;

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Test
    @DisplayName("Validation_예외_400_반환")
    void handleValidationException_Returns400() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("userDto", "email", "이메일 형식이 올바르지 않습니다");
        FieldError fieldError2 = new FieldError("userDto", "password", "비밀번호는 8자 이상이어야 합니다");

        given(bindingResult.getAllErrors()).willReturn(List.of(fieldError1, fieldError2));

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        given(exception.getBindingResult()).willReturn(bindingResult);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getSuccess()).isEqualTo(false);
        assertThat(body.getErrorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(body.getMessage()).isEqualTo("입력값 검증 실패");
        assertThat(body.getDetails()).hasSize(2);
        assertThat(body.getDetails().get("email")).isEqualTo("이메일 형식이 올바르지 않습니다");
        assertThat(body.getDetails().get("password")).isEqualTo("비밀번호는 8자 이상이어야 합니다");
    }

    @Test
    @DisplayName("UserNotFound_예외_404_반환")
    void handleBusinessException_UserNotFound_Returns404() {
        UserNotFoundException exception = new UserNotFoundException(1L);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("U001");
        assertThat(response.getBody().getMessage()).contains("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("BookNotFound_예외_404_반환")
    void handleBusinessException_BookNotFound_Returns404() {
        BookNotFoundException exception = new BookNotFoundException("ID: 999");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("B001");
    }

    @Test
    @DisplayName("TransactionNotFound_예외_404_반환")
    void handleBusinessException_TransactionNotFound_Returns404() {
        TransactionNotFoundException exception = new TransactionNotFoundException(123L);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("T001");
    }

    @Test
    @DisplayName("AccountNotFound_예외_404_반환")
    void handleBusinessException_AccountNotFound_Returns404() {
        AccountNotFoundException exception = new AccountNotFoundException(456L);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("A001");
    }

    @Test
    @DisplayName("Unauthorized_예외_401_반환")
    void handleBusinessException_Unauthorized_Returns401() {
        UnauthorizedException exception = new UnauthorizedException("잘못된 토큰");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("AUTH001");
        assertThat(response.getBody().getMessage()).isEqualTo("인증에 실패했습니다: 잘못된 토큰");
    }

    @Test
    @DisplayName("UnauthorizedBookAccess_예외_403_반환")
    void handleBusinessException_UnauthorizedBookAccess_Returns403() {
        UnauthorizedBookAccessException exception = new UnauthorizedBookAccessException(10L);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("B003");
    }

    @Test
    @DisplayName("DuplicateEmail_예외_400_반환")
    void handleBusinessException_DuplicateEmail_Returns400() {
        DuplicateEmailException exception = new DuplicateEmailException("test@test.com");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("U002");
    }

    @Test
    @DisplayName("InvalidTransaction_예외_400_반환")
    void handleBusinessException_InvalidTransaction_Returns400() {
        InvalidTransactionException exception = new InvalidTransactionException("차변과 대변이 일치하지 않습니다");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("T002");
    }

    @Test
    @DisplayName("DuplicateBookType_예외_400_반환")
    void handleBusinessException_DuplicateBookType_Returns400() {
        DuplicateBookTypeException exception = new DuplicateBookTypeException();

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("B002");
    }

    @Test
    @DisplayName("RateLimitExceeded_예외_429_반환")
    void handleBusinessException_RateLimitExceeded_Returns429() {
        RateLimitExceededException exception = new RateLimitExceededException("1분당 10회 제한");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("AUTH002");
    }

    @Test
    @DisplayName("AiServiceError_예외_503_반환")
    void handleBusinessException_AiServiceError_Returns503() {
        AiServiceException exception = new AiServiceException("Gemini API 호출 실패");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("AI001");
        assertThat(response.getBody().getMessage()).isEqualTo("AI 서비스 호출에 실패했습니다: Gemini API 호출 실패");
    }

    @Test
    @DisplayName("AiParsingError_예외_503_반환")
    void handleBusinessException_AiParsingError_Returns503() {
        AiParsingException exception = new AiParsingException("JSON 파싱 실패");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("AI002");
    }

    @Test
    @DisplayName("SessionNotFound_예외_503_반환")
    void handleBusinessException_SessionNotFound_Returns503() {
        SessionNotFoundException exception = new SessionNotFoundException("session-123");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("AI003");
    }

    @Test
    @DisplayName("일반_예외_테스트환경_상세메시지_노출")
    void handleException_TestEnvironment_ShowsDetailMessage() {
        given(environment.getActiveProfiles()).willReturn(new String[]{"test"});
        Exception exception = new RuntimeException("데이터베이스 연결 실패");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(response.getBody().getMessage()).contains("서버 오류:", "데이터베이스 연결 실패");
    }

    @Test
    @DisplayName("일반_예외_운영환경_상세메시지_숨김")
    void handleException_ProdEnvironment_HidesDetailMessage() {
        given(environment.getActiveProfiles()).willReturn(new String[]{"prod"});
        Exception exception = new RuntimeException("민감한 정보");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("서버 오류가 발생했습니다.")
                .doesNotContain("민감한 정보");
    }

    @Test
    @DisplayName("일반_예외_dev환경_상세메시지_노출")
    void handleException_DevEnvironment_ShowsDetailMessage() {
        given(environment.getActiveProfiles()).willReturn(new String[]{"dev"});
        Exception exception = new NullPointerException("객체 참조 오류");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleException(exception);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("객체 참조 오류");
    }
}
