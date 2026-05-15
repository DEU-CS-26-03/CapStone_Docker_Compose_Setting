package com.capstone.storage.dto;

import lombok.*;

@Getter
@Setter // ★ 추가: 서비스 레이어의 호출 및 데이터 바인딩을 위해 필요
@Builder
@NoArgsConstructor
@AllArgsConstructor // ★ 중요: Builder가 작동하기 위해 필수
public class PresignRequest {
    private String fileName;
    private String contentType;
    private String uploadType;
}