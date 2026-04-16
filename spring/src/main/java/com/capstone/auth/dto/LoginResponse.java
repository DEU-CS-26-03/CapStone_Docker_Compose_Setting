package com.capstone.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String tokenType;
    private UserInfo user;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String email;
        private String nickname;
        private String role;
    }
}