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
                User admin = new User();
                admin.setEmail(adminEmail);
                admin.setPasswordHash(passwordEncoder.encode("Admin1234!"));
                admin.setNickname("관리자");
                admin.setRole(UserRole.ADMIN);
                admin.setStatus(UserStatus.ACTIVE);

                userRepository.save(admin);
                System.out.println("[Seed] 관리자 계정 생성 완료: " + adminEmail);
            }
        };
    }
}