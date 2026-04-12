package com.capstone.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MeResponse {
    private Long id;
    private String email;
    private String nickname;
    private String profileImageUrl;
}