//비즈니스 로그인 로직 분리
package com.capstone.auth.service;

import com.capstone.auth.dto.LoginRequest;
import com.capstone.auth.dto.RegisterRequest;
import com.capstone.auth.dto.RegisterResponse;
import com.capstone.auth.dto.TokenResponse;
import com.capstone.user.entity.User;
import com.capstone.user.repository.UserRepository;
import com.capstone.security.jwt.JwtTokenProvider;
import com.capstone.user.dto.MeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        String nickname = normalizeNickname(request.getNickname());
        String password = request.getPassword();

        if (email.isBlank() || password == null || password.isBlank() || nickname.isBlank()) {
            throw new IllegalArgumentException("필수 값이 누락되었습니다.");
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);

        User saved = userRepository.save(user);
        return new RegisterResponse("회원가입 성공", saved.getId());
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        String rawPassword = request.getPassword();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createToken(user.getId(), user.getEmail());

        return new TokenResponse(
                "로그인 성공",
                accessToken,
                new TokenResponse.UserSummary(user.getId(), user.getNickname())
        );
    }

    @Transactional(readOnly = true)
    public MeResponse getMe(String email) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return new MeResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl()
        );
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String normalizeNickname(String nickname) {
        return nickname == null ? "" : nickname.trim();
    }
}