package com.capstone.storage.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresignResponse {
    private String uploadUrl;
    private String uploadToken;
    private String objectKey;
    private int expiresIn;
    private String method;
}