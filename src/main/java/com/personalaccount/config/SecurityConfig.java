package com.personalaccount.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정
 *
 * 현재 상태: JWT 구현 전 임시 설정
 * - 모든 요청 허용 (개발용)
 * - JWT 필터 추가 후 인증 활성화 예정
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
                        // TODO: JWT 구현 후 인증 활성화
                        // 현재는 개발 편의를 위해 모든 요청 허용
                        .anyRequest().permitAll()
                )

                // 세션 사용 안 함 (JWT 사용 예정)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }
}