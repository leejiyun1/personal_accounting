package com.personalaccount.presentation;

import com.personalaccount.application.auth.dto.request.LoginRequest;
import com.personalaccount.application.auth.dto.request.RefreshRequest;
import com.personalaccount.application.auth.dto.response.LoginResponse;
import com.personalaccount.application.auth.service.AuthService;
import com.personalaccount.common.dto.CommonResponse;
import com.personalaccount.common.dto.ResponseFactory;
import com.personalaccount.common.exception.custom.UnauthorizedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 API")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "로그인",
            description = """
                이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.
                **Rate Limit**: 1분당 5회
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "이메일 또는 비밀번호가 일치하지 않습니다"
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "로그인 시도 횟수 초과 (1분 후 재시도)"
            )
    })
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        log.info("POST /api/v1/auth/login");
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ResponseFactory.success(response, "로그인 성공"));
    }

    @Operation(
            summary = "토큰 갱신",
            description = """
                Refresh Token으로 새로운 Access Token과 Refresh Token을 발급받습니다.
                **Refresh Token Rotation**: 기존 Refresh Token은 자동으로 무효화됩니다.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 갱신 성공",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "유효하지 않은 리프레시 토큰입니다"
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<CommonResponse<LoginResponse>> refresh(
            @Valid @RequestBody RefreshRequest request
    ) {
        log.info("POST /api/v1/auth/refresh");
        LoginResponse response = authService.refresh(request);
        return ResponseEntity.ok(ResponseFactory.success(response, "토큰 갱신 성공"));
    }

    @Operation(
            summary = "로그아웃",
            description = "Access Token을 블랙리스트에 추가하여 무효화합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패"
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<Void>> logout(
            @RequestHeader("Authorization") String authorization
    ) {
        log.info("POST /api/v1/auth/logout");
        String accessToken = extractBearerToken(authorization);
        authService.logout(accessToken);
        return ResponseEntity.ok(ResponseFactory.successWithMessage("로그아웃 성공"));
    }

    private String extractBearerToken(String authorization) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            throw new UnauthorizedException("Bearer 토큰 형식이 아닙니다");
        }
        return authorization.substring(7);
    }
}
