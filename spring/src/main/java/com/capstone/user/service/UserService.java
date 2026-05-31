package com.capstone.user.service;

import com.capstone.user.dto.MeResponse;
import com.capstone.user.dto.PasswordUpdateRequest;
import com.capstone.user.dto.ProfileUpdateRequest;
import com.capstone.user.entity.User;
import com.capstone.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 1. 내 정보 조회
    @Transactional(readOnly = true)
    public MeResponse getMe(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return new MeResponse(user.getId(), user.getEmail(), user.getNickname(), user.getProfileImageUrl());
    }

    // 2. 내 정보 수정 (닉네임/이메일)
    @Transactional
    public void updateProfile(String currentEmail, ProfileUpdateRequest request) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (request.getNickname() != null && !request.getNickname().isBlank()) {
            user.setNickname(request.getNickname()); // 엔티티에 Setter가 있어야 합니다.
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail());
        }
        userRepository.save(user);
    }

    // 3. 비밀번호 변경 (핵심 로직)
    @Transactional
    public void updatePassword(String email, PasswordUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 기존 비밀번호가 맞는지 검사 (BCrypt 매칭)
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("기존 비밀번호가 일치하지 않습니다.");
        }

        // 맞으면 새 비밀번호를 암호화해서 덮어씌움
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}