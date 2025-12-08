package com.personalaccount.auth.service;

import com.personalaccount.auth.dto.request.LoginRequest;
import com.personalaccount.auth.dto.request.RefreshRequest;
import com.personalaccount.auth.dto.response.LoginResponse;
import com.personalaccount.auth.jwt.JwtTokenProvider;
import com.personalaccount.auth.service.impl.AuthServiceImpl;
import com.personalaccount.common.exception.custom.UnauthorizedException;
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

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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

        // LoginRequest 설정 (Reflection 사용)
        loginRequest = new LoginRequest();
        try {
            java.lang.reflect.Field emailField = LoginRequest.class.getDeclaredField("email");
            emailField.setAccessible(true);
            emailField.set(loginRequest, "test@test.com");

            java.lang.reflect.Field passwordField = LoginRequest.class.getDeclaredField("password");
            passwordField.setAccessible(true);
            passwordField.set(loginRequest, "password123");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("로그인_성공_토큰생성확인")
    void login_Success() {
        // Given
        given(rateLimitService.tryConsume(any(), anyString())).willReturn(true);
        given(userRepository.findByEmail(loginRequest.getEmail())).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(jwtTokenProvider.createAccessToken(anyLong(), anyString())).willReturn("accessToken");
        given(jwtTokenProvider.createRefreshToken(anyLong())).willReturn("refreshToken");

        // When
        LoginResponse result = authService.login(loginRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("accessToken");
        assertThat(result.getRefreshToken()).isEqualTo("refreshToken");
        assertThat(result.getUser()).isNotNull();
        assertThat(result.getUser().getId()).isEqualTo(1L);
        assertThat(result.getUser().getEmail()).isEqualTo("test@test.com");

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(anyString(), anyString());
        verify(jwtTokenProvider).createAccessToken(anyLong(), anyString());
        verify(jwtTokenProvider).createRefreshToken(anyLong());
    }

    @Test
    @DisplayName("로그인_사용자없음_예외발생")
    void login_UserNotFound_ThrowsException() {
        // Given
        given(rateLimitService.tryConsume(any(), anyString())).willReturn(true);
        given(userRepository.findByEmail(loginRequest.getEmail())).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("이메일 또는 비밀번호가 일치하지 않습니다");

        verify(userRepository).findByEmail(loginRequest.getEmail());
    }

    @Test
    @DisplayName("로그인_비밀번호불일치_예외발생")
    void login_WrongPassword_ThrowsException() {
        // Given
        given(rateLimitService.tryConsume(any(), anyString())).willReturn(true);
        given(userRepository.findByEmail(loginRequest.getEmail())).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("이메일 또는 비밀번호가 일치하지 않습니다");

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("토큰갱신_성공")
    void refresh_Success() {
        // Given
        RefreshRequest refreshRequest = new RefreshRequest();
        try {
            java.lang.reflect.Field refreshTokenField = RefreshRequest.class.getDeclaredField("refreshToken");
            refreshTokenField.setAccessible(true);
            refreshTokenField.set(refreshRequest, "validRefreshToken");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        given(jwtTokenProvider.validateToken("validRefreshToken")).willReturn(true);
        given(jwtTokenProvider.getUserId("validRefreshToken")).willReturn(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(jwtTokenProvider.createAccessToken(1L, "test@test.com")).willReturn("newAccessToken");

        // When
        LoginResponse result = authService.refresh(refreshRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("newAccessToken");
        assertThat(result.getRefreshToken()).isEqualTo("validRefreshToken");
        assertThat(result.getUser()).isNotNull();
        assertThat(result.getUser().getId()).isEqualTo(1L);

        verify(jwtTokenProvider).validateToken("validRefreshToken");
        verify(jwtTokenProvider).getUserId("validRefreshToken");
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("토큰갱신_유효하지않은토큰_예외발생")
    void refresh_InvalidToken_ThrowsException() {
        // Given
        RefreshRequest refreshRequest = new RefreshRequest();
        try {
            java.lang.reflect.Field refreshTokenField = RefreshRequest.class.getDeclaredField("refreshToken");
            refreshTokenField.setAccessible(true);
            refreshTokenField.set(refreshRequest, "invalidRefreshToken");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        given(jwtTokenProvider.validateToken("invalidRefreshToken")).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.refresh(refreshRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("유효하지 않은 리프레시 토큰입니다");

        verify(jwtTokenProvider).validateToken("invalidRefreshToken");
    }

    @Test
    @DisplayName("로그아웃_성공_블랙리스트추가확인")
    void logout_Success() {
        // Given
        String accessToken = "validAccessToken";
        Long userId = 1L;
        Long expiration = 900000L; // 15분

        given(jwtTokenProvider.getUserId(accessToken)).willReturn(userId);
        given(jwtTokenProvider.getExpiration(accessToken)).willReturn(expiration);
        given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);

        // When
        authService.logout(accessToken);

        // Then
        verify(jwtTokenProvider).getUserId(accessToken);
        verify(jwtTokenProvider).getExpiration(accessToken);
        verify(valueOperations).set(
                eq("blacklist:" + accessToken),
                eq("logout"),
                eq(expiration),
                eq(TimeUnit.MILLISECONDS)
        );
    }

}