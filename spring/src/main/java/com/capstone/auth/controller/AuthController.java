//로그인 API 제공, 인증 성공 시 TokenResponse 반환.

package com.capstone.auth.controller;

import com.capstone.auth.dto.LoginRequest;
import com.capstone.auth.dto.RegisterRequest;
import com.capstone.auth.dto.RegisterResponse;
import com.capstone.auth.dto.TokenResponse;
import com.capstone.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}