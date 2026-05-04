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
@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 정적 파일 공개 - GET/HEAD 포함 전체 허용
                        .requestMatchers("/files/**", "/uploads/**", "/results/**").permitAll()

                        // 공개 API
                        .requestMatchers(
                                "/error",
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/api/v1/health",
                                "/api/v1/models/status"
                        ).permitAll()

                        // 의류 목록/상세 비로그인 조회 허용
                        .requestMatchers(HttpMethod.GET, "/api/v1/garments/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/brands/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/catalog/items/**").permitAll()

                        // 내부 워커 API
                        .requestMatchers("/api/internal/**").permitAll()

                        // 관리자/판매자 전용
                        .requestMatchers(HttpMethod.POST, "/api/v1/catalog/import/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/catalog/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/catalog/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/catalog/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/v1/garments/**").hasAnyRole("ADMIN", "SELLER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/garments/**").hasAnyRole("ADMIN", "SELLER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/garments/**").hasAnyRole("ADMIN", "SELLER")

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
