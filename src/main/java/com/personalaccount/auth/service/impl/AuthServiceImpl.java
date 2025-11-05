package com.personalaccount.auth.service.impl;

import com.personalaccount.auth.dto.request.LoginRequest;
import com.personalaccount.auth.dto.request.RefreshRequest;
import com.personalaccount.auth.dto.response.LoginResponse;
import com.personalaccount.auth.jwt.JwtTokenProvider;
import com.personalaccount.auth.service.AuthService;
import com.personalaccount.common.exception.custom.RateLimitExceededException;
import com.personalaccount.common.exception.custom.UnauthorizedException;
import com.personalaccount.common.exception.custom.UserNotFoundException;
import com.personalaccount.common.ratelimit.RateLimitService;
import com.personalaccount.common.ratelimit.RateLimitService.KeyType;
import com.personalaccount.common.util.LogMaskingUtil;
import java.util.concurrent.TimeUnit;
import com.personalaccount.domain.user.entity.User;
import com.personalaccount.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RateLimitService rateLimitService;
    private final StringRedisTemplate stringRedisTemplate; // Spring Boot 자동 설정 빈 사용

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("로그인 시도: email={}", LogMaskingUtil.maskEmail(request.getEmail()));

        // Rate Limit 체크
        if (!rateLimitService.tryConsume(KeyType.LOGIN, request.getEmail())) {
            throw new RateLimitExceededException(
                    "로그인 시도 횟수를 초과했습니다. 1분 후 다시 시도해주세요."
            );
        }

        // 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("이메일 또는 비밀번호가 일치하지 않습니다"));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("이메일 또는 비밀번호가 일치하지 않습니다");
        }

        // 로그인 성공 시 Rate Limit 리셋
        rateLimitService.reset(KeyType.LOGIN, request.getEmail());

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        log.info("로그인 성공: userId={}, email={}",
                user.getId(),
                LogMaskingUtil.maskEmail(user.getEmail()));

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

    @Override
    @Transactional
    public LoginResponse refresh(RefreshRequest request) {
        log.info("토큰 갱신 시도");

        // Refresh Token 검증
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new UnauthorizedException("유효하지 않은 리프레시 토큰입니다");
        }

        Long userId = jwtTokenProvider.getUserId(request.getRefreshToken());

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 새로운 Access Token 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());

        log.info("토큰 갱신 성공: userId={}", userId);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .name(user.getName())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public void logout(String accessToken) {
        Long userId = jwtTokenProvider.getUserId(accessToken);

        // 토큰을 블랙리스트에 추가 (만료 시간까지)
        long expiration = jwtTokenProvider.getExpiration(accessToken);
        stringRedisTemplate.opsForValue().set(
                "blacklist:" + accessToken,
                "logout",
                expiration,
                TimeUnit.MILLISECONDS
        );

        log.info("로그아웃 완료: userId={}", userId);
    }
}