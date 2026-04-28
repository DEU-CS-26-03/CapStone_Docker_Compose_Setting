package com.capstone.garment.dto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GarmentCreateRequest {

    @NotBlank
    private String filename;        // 파일명 (또는 상품명)

    private String category;        // 카테고리 코드 (예: "TOP", "BOTTOM")
    private String brandKey;        // 브랜드 식별자
    private String sourceType;      // "UPLOAD", "IMPORT" 등

    private String imageUrl;        // 이미 저장된 이미지 URL (선택)
    private String description;
}