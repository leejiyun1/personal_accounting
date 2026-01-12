package com.personalaccount.infrastructure.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter 테스트")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한_토큰_인증_성공")
    void doFilterInternal_ValidToken_SetsAuthentication() throws ServletException, IOException {
        String validToken = "valid.jwt.token";
        Long userId = 1L;

        given(request.getHeader("Authorization")).willReturn("Bearer " + validToken);
        given(jwtTokenProvider.validateToken(validToken)).willReturn(true);
        given(stringRedisTemplate.hasKey("blacklist:" + validToken)).willReturn(false);
        given(jwtTokenProvider.getTokenType(validToken)).willReturn("access");
        given(jwtTokenProvider.getUserId(validToken)).willReturn(userId);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(userId);
        assertThat(authentication.getAuthorities()).isEmpty();

        verify(jwtTokenProvider).validateToken(validToken);
        verify(jwtTokenProvider).getTokenType(validToken);
        verify(jwtTokenProvider).getUserId(validToken);
        verify(stringRedisTemplate).hasKey("blacklist:" + validToken);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("블랙리스트_토큰_인증_실패")
    void doFilterInternal_BlacklistedToken_DoesNotSetAuthentication() throws ServletException, IOException {
        String blacklistedToken = "blacklisted.jwt.token";

        given(request.getHeader("Authorization")).willReturn("Bearer " + blacklistedToken);
        given(jwtTokenProvider.validateToken(blacklistedToken)).willReturn(true);
        given(stringRedisTemplate.hasKey("blacklist:" + blacklistedToken)).willReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(jwtTokenProvider).validateToken(blacklistedToken);
        verify(stringRedisTemplate).hasKey("blacklist:" + blacklistedToken);
        verify(jwtTokenProvider, never()).getUserId(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("유효하지않은_토큰_인증_실패")
    void doFilterInternal_InvalidToken_DoesNotSetAuthentication() throws ServletException, IOException {
        String invalidToken = "invalid.jwt.token";

        given(request.getHeader("Authorization")).willReturn("Bearer " + invalidToken);
        given(jwtTokenProvider.validateToken(invalidToken)).willReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(jwtTokenProvider).validateToken(invalidToken);
        verify(stringRedisTemplate, never()).hasKey(anyString());
        verify(jwtTokenProvider, never()).getUserId(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("토큰_없음_인증_안함")
    void doFilterInternal_NoToken_DoesNotSetAuthentication() throws ServletException, IOException {
        given(request.getHeader("Authorization")).willReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(stringRedisTemplate, never()).hasKey(anyString());
        verify(jwtTokenProvider, never()).getUserId(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Bearer_없는_토큰_인증_안함")
    void doFilterInternal_TokenWithoutBearer_DoesNotSetAuthentication() throws ServletException, IOException {
        given(request.getHeader("Authorization")).willReturn("InvalidPrefix token");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("빈_Authorization_헤더_인증_안함")
    void doFilterInternal_EmptyAuthorizationHeader_DoesNotSetAuthentication() throws ServletException, IOException {
        given(request.getHeader("Authorization")).willReturn("");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("토큰_파싱_예외_발생_인증_안함")
    void doFilterInternal_ExceptionDuringParsing_DoesNotSetAuthentication() throws ServletException, IOException {
        String validToken = "valid.jwt.token";

        given(request.getHeader("Authorization")).willReturn("Bearer " + validToken);
        given(jwtTokenProvider.validateToken(validToken)).willReturn(true);
        given(stringRedisTemplate.hasKey("blacklist:" + validToken)).willReturn(false);
        given(jwtTokenProvider.getTokenType(validToken)).willReturn("access");
        given(jwtTokenProvider.getUserId(validToken)).willThrow(new RuntimeException("Parsing error"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Bearer_토큰_정상_추출")
    void extractToken_ValidBearerToken_ExtractsToken() throws ServletException, IOException {
        String token = "my.jwt.token";
        given(request.getHeader("Authorization")).willReturn("Bearer " + token);
        given(jwtTokenProvider.validateToken(token)).willReturn(true);
        given(stringRedisTemplate.hasKey("blacklist:" + token)).willReturn(false);
        given(jwtTokenProvider.getTokenType(token)).willReturn("access");
        given(jwtTokenProvider.getUserId(token)).willReturn(1L);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtTokenProvider).validateToken(token);
    }

    @Test
    @DisplayName("블랙리스트_체크_null_반환_정상_처리")
    void isBlacklisted_NullReturnedFromRedis_TreatedAsFalse() throws ServletException, IOException {
        String token = "token";
        given(request.getHeader("Authorization")).willReturn("Bearer " + token);
        given(jwtTokenProvider.validateToken(token)).willReturn(true);
        given(stringRedisTemplate.hasKey("blacklist:" + token)).willReturn(null);
        given(jwtTokenProvider.getTokenType(token)).willReturn("access");
        given(jwtTokenProvider.getUserId(token)).willReturn(1L);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Refresh_토큰으로_접근_시도_인증_실패")
    void doFilterInternal_RefreshToken_DoesNotSetAuthentication() throws ServletException, IOException {
        String refreshToken = "refresh.jwt.token";

        given(request.getHeader("Authorization")).willReturn("Bearer " + refreshToken);
        given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true);
        given(stringRedisTemplate.hasKey("blacklist:" + refreshToken)).willReturn(false);
        given(jwtTokenProvider.getTokenType(refreshToken)).willReturn("refresh");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(jwtTokenProvider, never()).getUserId(anyString());
        verify(filterChain).doFilter(request, response);
    }
}