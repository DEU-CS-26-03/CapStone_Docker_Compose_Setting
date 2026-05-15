package com.capstone.storage.dto;

import lombok.*;

@Getter
@Setter
@Builder // ★ 에러 로그의 'cannot find symbol: method builder()' 해결
@NoArgsConstructor
@AllArgsConstructor
public class PresignResponse {
    private String uploadUrl;
    private String uploadToken;
    private String objectKey;
    private int expiresIn;
    private String method;
}