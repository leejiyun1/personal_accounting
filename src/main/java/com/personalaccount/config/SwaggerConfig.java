package com.personalaccount.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Personal Accounting API")
                        .description("AI 대화형 복식부기 가계부 API\n\n" +
                                "## 주요 기능\n" +
                                "- 복식부기 자동 생성 (차변/대변 자동 계산)\n" +
                                "- AI 자연어 처리 거래 입력\n" +
                                "- 재무제표 자동 생성 (손익계산서, 재무상태표)\n" +
                                "- 월별/카테고리별 통계\n\n" +
                                "## 인증\n" +
                                "JWT Bearer Token 방식을 사용합니다.\n" +
                                "1. `/api/v1/auth/login`으로 로그인\n" +
                                "2. 받은 `accessToken`을 우측 상단 Authorize 버튼에 입력")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Lee Jiyun")
                                .email("poi20701556@gmail.com")
                                .url("https://github.com/leejiyun1"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.personal-accounting.com")
                                .description("Production Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT 인증 토큰을 입력하세요. (Bearer prefix 제외)")));
    }
}