package com.capstone.config;

import com.capstone.user.entity.User;
import com.capstone.user.entity.UserRole;
import com.capstone.user.entity.UserStatus;
import com.capstone.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AdminSeedConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedAdmin() {
        return args -> {
            String adminEmail = "admin@test.com";

            if (userRepository.findByEmail(adminEmail).isEmpty()) {
                User admin = User.builder()
                        .email(adminEmail)
                        .passwordHash(passwordEncoder.encode("Admin1234!"))
                        .nickname("관리자")
                        .role(UserRole.ADMIN)
                        .status(UserStatus.ACTIVE)
                        .build();

                userRepository.save(admin);
                System.out.println("[AdminSeed] 관리자 계정 생성 완료: " + adminEmail);
            }
        };
    }
}