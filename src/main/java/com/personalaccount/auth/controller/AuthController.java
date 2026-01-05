package com.personalaccount.auth.controller;

import com.personalaccount.auth.dto.request.LoginRequest;
import com.personalaccount.auth.dto.request.RefreshRequest;
import com.personalaccount.auth.dto.response.LoginResponse;
import com.personalaccount.auth.service.AuthService;
import com.personalaccount.common.dto.CommonResponse;
import com.personalaccount.common.dto.ResponseFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<CommonResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        log.info("POST /api/v1/auth/login");
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ResponseFactory.success(response, "로그인 성공"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<CommonResponse<LoginResponse>> refresh(
            @Valid @RequestBody RefreshRequest request
    ) {
        log.info("POST /api/v1/auth/refresh");
        LoginResponse response = authService.refresh(request);
        return ResponseEntity.ok(ResponseFactory.success(response, "토큰 갱신 성공"));
    }

    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<Void>> logout(
            @RequestHeader("Authorization") String authorization
    ) {
        log.info("POST /api/v1/auth/logout");
        String accessToken = authorization.replace("Bearer ", "");
        authService.logout(accessToken);
        return ResponseEntity.ok(ResponseFactory.successWithMessage("로그아웃 성공"));
    }
}
