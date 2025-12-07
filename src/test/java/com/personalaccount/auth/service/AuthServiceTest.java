package com.personalaccount.auth.service;

import com.personalaccount.auth.dto.request.LoginRequest;
import com.personalaccount.auth.jwt.JwtTokenProvider;
import com.personalaccount.auth.service.impl.AuthServiceImpl;
import com.personalaccount.common.ratelimit.RateLimitService;
import com.personalaccount.domain.user.entity.User;
import com.personalaccount.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 테스트")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .password("encodedPassword")
                .name("테스터")
                .isActive(true)
                .build();

        loginRequest = new LoginRequest();
        // LoginRequest는 @NoArgsConstructor만 있어서 reflection으로 설정 필요
        // 또는 테스트용 생성자 추가
    }

    @Test
    @DisplayName("로그인_성공_토큰생성확인")
    void login_Success() {
        // Given

        // When

        // Then
    }

    @Test
    @DisplayName("로그인_사용자없음_예외발생")
    void login_UserNotFound_ThrowsException() {
        // Given

        // When & Then
    }

    @Test
    @DisplayName("로그인_비밀번호불일치_예외발생")
    void login_WrongPassword_ThrowsException() {
        // Given

        // When & Then
    }

    @Test
    @DisplayName("토큰갱신_성공")
    void refresh_Success() {
        // Given

        // When

        // Then
    }

    @Test
    @DisplayName("토큰갱신_유효하지않은토큰_예외발생")
    void refresh_InvalidToken_ThrowsException() {
        // Given

        // When & Then
    }

    @Test
    @DisplayName("로그아웃_성공_블랙리스트추가확인")
    void logout_Success() {
        // Given

        // When

        // Then
    }
}