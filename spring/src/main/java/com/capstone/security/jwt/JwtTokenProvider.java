package com.capstone.security.jwt;

import com.capstone.user.service.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final CustomUserDetailsService userDetailsService;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    // ★ 수정됨: 권한(role)을 추가로 파라미터로 받도록 변경
    public String createToken(Long userId, String email, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getExpiration());

        var builder = Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey());

        if (userId != null) {
            builder.claim("userId", userId);
        }

        // ★ 핵심 추가: 프론트엔드가 권한을 확인할 수 있도록 토큰에 role 심기
        if (role != null && !role.isBlank()) {
            builder.claim("role", role);
        }

        return builder.compact();
    }

    // 기존 코드 호환용 (권한 없는 단순 토큰 생성 시)
    public String generateToken(String username) {
        return createToken(null, username, "USER"); // 기본값 USER
    }

    // 토큰에서 email(subject) 추출
    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    // 토큰에서 userId 추출
    public Long getUserId(String token) {
        Object value = parseClaims(token).get("userId");
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        return Long.parseLong(String.valueOf(value));
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Authentication 객체 반환
    public Authentication getAuthentication(String token) {
        String email = getUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}