package com.personalaccount.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "로그인 응답")
@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {

    @Schema(description = "액세스 토큰 (유효기간: 15분)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "리프레시 토큰 (유효기간: 7일)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    @Schema(description = "사용자 정보")
    private UserInfo user;

    @Schema(description = "사용자 정보")
    @Getter
    @Builder
    @AllArgsConstructor
    public static class UserInfo {
        @Schema(description = "사용자 ID", example = "1")
        private Long id;

        @Schema(description = "이메일", example = "test@test.com")
        private String email;

        @Schema(description = "이름", example = "홍길동")
        private String name;
    }
}