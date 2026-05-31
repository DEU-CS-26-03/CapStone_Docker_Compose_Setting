package com.capstone.user.controller;

import com.capstone.user.dto.MeResponse;
import com.capstone.user.dto.PasswordUpdateRequest;
import com.capstone.user.dto.ProfileUpdateRequest;
import com.capstone.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth") // ★ 프론트엔드 통신 주소와 완벽 일치시킴
@RequiredArgsConstructor
public class MeController {

    private final UserService userService;

    // 1. 내 정보 조회 (하드코딩 제거 완료)
    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(Authentication authentication) {
        String email = authentication.getName();
        MeResponse response = userService.getMe(email);
        return ResponseEntity.ok(response);
    }

    // 2. 내 정보 수정
    @PatchMapping("/me")
    public ResponseEntity<?> updateProfile(@RequestBody ProfileUpdateRequest request, Authentication authentication) {
        String email = authentication.getName();
        userService.updateProfile(email, request);
        return ResponseEntity.ok(Map.of("message", "기본 정보가 성공적으로 변경되었습니다."));
    }

    // 3. 비밀번호 변경
    @PatchMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody PasswordUpdateRequest request, Authentication authentication) {
        String email = authentication.getName();
        userService.updatePassword(email, request);
        return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
    }
}