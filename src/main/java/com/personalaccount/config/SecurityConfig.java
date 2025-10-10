package com.personalaccount.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정
 * 목적:
 * - URL별 접근 권한 설정
 * - 회원가입/로그인은 누구나 접근 가능
 * - 나머지 API는 인증 필요
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (REST API는 필요 없음)
                .csrf(csrf -> csrf.disable())

                // URL별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 인증 API - 누구나 접근 가능
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // Swagger - 누구나 접근 가능 (개발 편의)
                        .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )

                // 세션 사용 안 함 (JWT 사용 예정)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }
}