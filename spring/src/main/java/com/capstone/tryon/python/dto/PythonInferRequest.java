// src/main/java/com/capstone/python/dto/PythonInferRequest.java
package com.capstone.python.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PythonInferRequest {
    private String userImagePath;       // 유저 이미지 URL 또는 로컬 경로
    private String garmentImagePath;    // 의류 이미지 URL 또는 로컬 경로
}