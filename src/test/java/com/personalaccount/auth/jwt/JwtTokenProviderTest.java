package com.personalaccount.auth.jwt;

import com.personalaccount.infrastructure.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtTokenProvider 테스트")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private SecretKey secretKey;
    private static final String SECRET = "test-secret-key-for-jwt-token-generation-must-be-at-least-256-bits";
    private static final long ACCESS_TOKEN_VALIDITY = 900000L;
    private static final long REFRESH_TOKEN_VALIDITY = 604800000L;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, ACCESS_TOKEN_VALIDITY, REFRESH_TOKEN_VALIDITY);
        secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("AccessToken_생성_성공")
    void createAccessToken_Success() {
        Long userId = 1L;
        String email = "test@test.com";

        String token = jwtTokenProvider.createAccessToken(userId, email);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        var claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("email", String.class)).isEqualTo("test@test.com");
        assertThat(claims.get("type", String.class)).isEqualTo("access");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
    }

    @Test
    @DisplayName("AccessToken_유효기간_15분_확인")
    void createAccessToken_ValidityPeriod() {
        Long userId = 1L;
        String email = "test@test.com";

        String token = jwtTokenProvider.createAccessToken(userId, email);

        var claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        long issuedAt = claims.getIssuedAt().getTime();
        long expiration = claims.getExpiration().getTime();
        long validity = expiration - issuedAt;

        assertThat(validity).isEqualTo(ACCESS_TOKEN_VALIDITY);
    }

    @Test
    @DisplayName("RefreshToken_생성_성공")
    void createRefreshToken_Success() {
        Long userId = 1L;

        String token = jwtTokenProvider.createRefreshToken(userId);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        var claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("type", String.class)).isEqualTo("refresh");
        assertThat(claims.get("email")).isNull();
    }

    @Test
    @DisplayName("RefreshToken_유효기간_7일_확인")
    void createRefreshToken_ValidityPeriod() {
        Long userId = 1L;

        String token = jwtTokenProvider.createRefreshToken(userId);

        var claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        long issuedAt = claims.getIssuedAt().getTime();
        long expiration = claims.getExpiration().getTime();
        long validity = expiration - issuedAt;

        assertThat(validity).isEqualTo(REFRESH_TOKEN_VALIDITY);
    }

    @Test
    @DisplayName("토큰에서_UserId_추출_성공")
    void getUserId_Success() {
        Long userId = 123L;
        String token = jwtTokenProvider.createAccessToken(userId, "test@test.com");

        Long extractedUserId = jwtTokenProvider.getUserId(token);

        assertThat(extractedUserId).isEqualTo(123L);
    }

    @Test
    @DisplayName("RefreshToken에서_UserId_추출_성공")
    void getUserId_FromRefreshToken_Success() {
        Long userId = 456L;
        String token = jwtTokenProvider.createRefreshToken(userId);

        Long extractedUserId = jwtTokenProvider.getUserId(token);

        assertThat(extractedUserId).isEqualTo(456L);
    }

    @Test
    @DisplayName("유효한_토큰_검증_성공")
    void validateToken_ValidToken_ReturnsTrue() {
        String token = jwtTokenProvider.createAccessToken(1L, "test@test.com");

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("잘못된_형식_토큰_검증_실패")
    void validateToken_InvalidFormat_ReturnsFalse() {
        String invalidToken = "invalid.token.format";

        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("빈_토큰_검증_실패")
    void validateToken_EmptyToken_ReturnsFalse() {
        boolean isValid = jwtTokenProvider.validateToken("");

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("null_토큰_검증_실패")
    void validateToken_NullToken_ReturnsFalse() {
        boolean isValid = jwtTokenProvider.validateToken(null);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("만료된_토큰_검증_실패")
    void validateToken_ExpiredToken_ReturnsFalse() throws InterruptedException {
        JwtTokenProvider expiredProvider = new JwtTokenProvider(SECRET, -1L, REFRESH_TOKEN_VALIDITY);
        String expiredToken = expiredProvider.createAccessToken(1L, "test@test.com");

        Thread.sleep(10);

        boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("잘못된_서명_토큰_검증_실패")
    void validateToken_WrongSignature_ReturnsFalse() {
        JwtTokenProvider anotherProvider = new JwtTokenProvider(
                "another-secret-key-that-is-different-from-original-key-must-be-256-bits",
                ACCESS_TOKEN_VALIDITY,
                REFRESH_TOKEN_VALIDITY
        );
        String tokenWithWrongSignature = anotherProvider.createAccessToken(1L, "test@test.com");

        boolean isValid = jwtTokenProvider.validateToken(tokenWithWrongSignature);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("토큰_남은_유효시간_조회_성공")
    void getExpiration_Success() {
        String token = jwtTokenProvider.createAccessToken(1L, "test@test.com");

        long expiration = jwtTokenProvider.getExpiration(token);

        assertThat(expiration).isGreaterThan(0);
        assertThat(expiration).isLessThanOrEqualTo(ACCESS_TOKEN_VALIDITY);
    }

    @Test
    @DisplayName("만료된_토큰_유효시간_0반환")
    void getExpiration_ExpiredToken_ReturnsZero() throws InterruptedException {
        JwtTokenProvider expiredProvider = new JwtTokenProvider(SECRET, -1000L, REFRESH_TOKEN_VALIDITY);
        String expiredToken = expiredProvider.createAccessToken(1L, "test@test.com");

        Thread.sleep(10);

        long expiration = jwtTokenProvider.getExpiration(expiredToken);

        assertThat(expiration).isEqualTo(0);
    }

    @Test
    @DisplayName("잘못된_토큰_유효시간_0반환")
    void getExpiration_InvalidToken_ReturnsZero() {
        String invalidToken = "invalid.token.format";

        long expiration = jwtTokenProvider.getExpiration(invalidToken);

        assertThat(expiration).isEqualTo(0);
    }

    @Test
    @DisplayName("null_토큰_유효시간_0반환")
    void getExpiration_NullToken_ReturnsZero() {
        long expiration = jwtTokenProvider.getExpiration(null);

        assertThat(expiration).isEqualTo(0);
    }

    @Test
    @DisplayName("토큰_생성_검증_파싱_전체_플로우")
    void fullTokenFlow_Success() {
        Long userId = 999L;
        String email = "integration@test.com";

        String accessToken = jwtTokenProvider.createAccessToken(userId, email);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);

        assertThat(jwtTokenProvider.validateToken(accessToken)).isTrue();
        assertThat(jwtTokenProvider.validateToken(refreshToken)).isTrue();

        assertThat(jwtTokenProvider.getUserId(accessToken)).isEqualTo(userId);
        assertThat(jwtTokenProvider.getUserId(refreshToken)).isEqualTo(userId);

        assertThat(jwtTokenProvider.getExpiration(accessToken)).isGreaterThan(0);
        assertThat(jwtTokenProvider.getExpiration(refreshToken)).isGreaterThan(0);
    }

    @Test
    @DisplayName("여러_사용자_토큰_독립성_보장")
    void multipleUsers_TokenIndependence() {
        String token1 = jwtTokenProvider.createAccessToken(1L, "user1@test.com");
        String token2 = jwtTokenProvider.createAccessToken(2L, "user2@test.com");
        String token3 = jwtTokenProvider.createAccessToken(3L, "user3@test.com");

        assertThat(jwtTokenProvider.getUserId(token1)).isEqualTo(1L);
        assertThat(jwtTokenProvider.getUserId(token2)).isEqualTo(2L);
        assertThat(jwtTokenProvider.getUserId(token3)).isEqualTo(3L);

        assertThat(jwtTokenProvider.validateToken(token1)).isTrue();
        assertThat(jwtTokenProvider.validateToken(token2)).isTrue();
        assertThat(jwtTokenProvider.validateToken(token3)).isTrue();
    }
}