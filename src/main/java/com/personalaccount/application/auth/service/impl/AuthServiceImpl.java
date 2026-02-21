package com.personalaccount.application.auth.service.impl;

import com.personalaccount.application.auth.dto.request.LoginRequest;
import com.personalaccount.application.auth.dto.request.RefreshRequest;
import com.personalaccount.application.auth.dto.response.LoginResponse;
import com.personalaccount.infrastructure.security.jwt.JwtTokenProvider;
import com.personalaccount.infrastructure.security.repository.RefreshTokenRepository;
import com.personalaccount.application.auth.service.AuthService;
import com.personalaccount.common.exception.custom.RateLimitExceededException;
import com.personalaccount.common.exception.custom.UnauthorizedException;
import com.personalaccount.common.exception.custom.UserNotFoundException;
import com.personalaccount.common.ratelimit.RateLimitService;
import com.personalaccount.common.ratelimit.RateLimitService.KeyType;
import com.personalaccount.common.util.LogMaskingUtil;
import com.personalaccount.domain.user.entity.User;
import com.personalaccount.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RateLimitService rateLimitService;
    private final StringRedisTemplate stringRedisTemplate;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("로그인 시도: email={}", LogMaskingUtil.maskEmail(request.getEmail()));

        // Rate Limit 검증
        if (!rateLimitService.tryConsume(KeyType.LOGIN, request.getEmail())) {
            throw new RateLimitExceededException(
                    "로그인 시도 횟수를 초과했습니다. 1분 후 다시 시도해주세요."
            );
        }

        // 사용자 검증
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("이메일 또는 비밀번호가 일치하지 않습니다"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("이메일 또는 비밀번호가 일치하지 않습니다");
        }

        // Rate Limit 초기화
        rateLimitService.reset(KeyType.LOGIN, request.getEmail());

        // 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        // Refresh Token Redis 저장
        refreshTokenRepository.save(
                user.getId(),
                refreshToken,
                jwtTokenProvider.getRefreshTokenTtl()
        );

        log.info("로그인 성공: userId={}", user.getId());

        return buildLoginResponse(accessToken, refreshToken, user);
    }

    @Override
    @Transactional
    public LoginResponse refresh(RefreshRequest request) {
        log.info("토큰 갱신 시도");

        String refreshToken = request.getRefreshToken();

        // 1. JWT 서명 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("유효하지 않은 리프레시 토큰입니다");
        }

        // 2. 토큰 타입 검증
        String tokenType = jwtTokenProvider.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new UnauthorizedException("리프레시 토큰이 아닙니다");
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);

        // 3. Redis 저장소 검증 (실제 발급된 토큰인지)
        if (!refreshTokenRepository.validate(userId, refreshToken)) {
            log.warn("저장소에 없는 Refresh Token 사용 시도: userId={}", userId);
            // 탈취 의심 → 해당 사용자의 모든 토큰 무효화
            refreshTokenRepository.delete(userId);
            throw new UnauthorizedException("유효하지 않은 리프레시 토큰입니다");
        }

        // 4. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 5. 새 토큰 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        // 6. 기존 Refresh Token 삭제 + 새 토큰 저장 (Rotation)
        refreshTokenRepository.save(
                user.getId(),
                newRefreshToken,
                jwtTokenProvider.getRefreshTokenTtl()
        );

        log.info("토큰 갱신 성공: userId={}", userId);

        return buildLoginResponse(newAccessToken, newRefreshToken, user);
    }

    @Override
    @Transactional
    public void logout(String accessToken) {
        if (!jwtTokenProvider.validateToken(accessToken)) {
            throw new UnauthorizedException("유효하지 않은 액세스 토큰입니다");
        }

        String tokenType = jwtTokenProvider.getTokenType(accessToken);
        if (!"access".equals(tokenType)) {
            throw new UnauthorizedException("액세스 토큰이 아닙니다");
        }

        Long userId = jwtTokenProvider.getUserId(accessToken);

        // Access Token 블랙리스트 등록
        long expiration = jwtTokenProvider.getExpiration(accessToken);
        stringRedisTemplate.opsForValue().set(
                "blacklist:" + accessToken,
                "logout",
                expiration,
                TimeUnit.MILLISECONDS
        );

        // Refresh Token 삭제
        refreshTokenRepository.delete(userId);

        log.info("로그아웃 완료: userId={}", userId);
    }

    private LoginResponse buildLoginResponse(String accessToken, String refreshToken, User user) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .name(user.getName())
                        .build())
                .build();
    }
}
