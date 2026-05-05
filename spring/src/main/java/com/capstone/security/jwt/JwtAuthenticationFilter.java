package com.capstone.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        return HttpMethod.OPTIONS.matches(method)
                || path.equals("/api/v1/auth/register")
                || path.equals("/api/v1/auth/login")
                || path.equals("/api/v1/health")
                || path.equals("/api/v1/models/status")
                || path.startsWith("/api/internal/")
                || path.startsWith("/files/")
                || path.startsWith("/uploads/")
                || path.startsWith("/results/")
                || path.startsWith("/error");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        String method = request.getMethod();
        String token = resolveToken(request);

        log.info("[JWT] {} {} | Authorization header exists: {}", method, path, token != null);

        if (StringUtils.hasText(token)) {
            try {
                boolean valid = jwtTokenProvider.validateToken(token);
                log.info("[JWT] token validation result: {}", valid);

                if (valid) {
                    Authentication authentication = jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("[JWT] authenticated user: {}", authentication.getName());
                }
            } catch (Exception e) {
                log.error("[JWT] authentication failed for {} {}: {}", method, path, e.getMessage(), e);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}