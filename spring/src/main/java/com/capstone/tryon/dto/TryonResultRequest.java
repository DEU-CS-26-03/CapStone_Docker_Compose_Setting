package com.capstone.tryon.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TryonResultRequest {

    private String jobId;           // 추론 작업 고유 ID
    private String resultImagePath; // 서버 저장 경로 또는 상대 URL
    private String status;          // "SUCCESS" | "FAILED"
    private String errorMessage;    // 실패 시 메시지 (nullable)
}