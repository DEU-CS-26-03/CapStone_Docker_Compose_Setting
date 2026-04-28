package com.capstone.config;

import com.capstone.security.handler.CustomAuthenticationEntryPoint;
import com.capstone.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ── 공개 경로 (인증 불필요) ────────────────────────
                        .requestMatchers(
                                "/error",
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/api/v1/health",
                                "/api/v1/models/status"
                        ).permitAll()

                        // 의류 목록/상세 비로그인 조회 허용
                        .requestMatchers(HttpMethod.GET, "/api/v1/garments/**").permitAll()

                        // 자체 카탈로그 카테고리·브랜드·상품 GET 공개
                        // (29CM 제거 후 /api/v1/categories, /api/v1/brands 로 통일)
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/brands/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/catalog/items/**").permitAll()

                        // 결과 이미지 정적 서빙 (nginx가 처리하지만 Spring fallback 허용)
                        .requestMatchers(HttpMethod.GET, "/results/**").permitAll()

                        // 내부 워커 API (Python 노트북 → Spring push 전용)
                        // TODO: 추후 Worker-Secret 헤더 검증 필터로 교체 권장
                        .requestMatchers("/api/internal/**").permitAll()

                        // ── 관리자/판매자 전용 ──────────────────────────────
                        // 카탈로그 import (CSV 업로드 등) - ADMIN만
                        .requestMatchers(HttpMethod.POST, "/api/v1/catalog/import/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/catalog/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,  "/api/v1/catalog/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/catalog/**").hasRole("ADMIN")

                        // 의류 상태 변경/삭제 - ADMIN, SELLER
                        .requestMatchers(HttpMethod.POST,   "/api/v1/garments/**").hasAnyRole("ADMIN", "SELLER")
                        .requestMatchers(HttpMethod.PATCH,  "/api/v1/garments/**").hasAnyRole("ADMIN", "SELLER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/garments/**").hasAnyRole("ADMIN", "SELLER")

                        // ── 그 외 모든 요청: 인증 필요 ─────────────────────
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(authenticationEntryPoint))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}