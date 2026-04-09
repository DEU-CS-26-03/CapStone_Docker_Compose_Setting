//Python 워커 전용 내부 API
package com.capstone.job.controller;

import com.capstone.job.dto.WorkerJobResponse;
import com.capstone.job.dto.WorkerStatusUpdateRequest;
import com.capstone.job.service.JobService;
import com.capstone.tryon.dto.TryonResponse;
import com.capstone.tryon.service.TryonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Python GPU 워커 전용 내부 API
 * - 외부 프론트엔드에서 호출하는 API가 아님
 * - SecurityConfig에서 /api/internal/** 경로 인증 처리 별도 설정 필요
 */
@RestController
@RequestMapping("/api/internal/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final TryonService tryonService;

    /**
     * GET /api/internal/jobs/next
     * Python 워커가 다음 처리할 작업을 가져감
     * - queued → processing 상태로 자동 전환
     * - 없으면 204 No Content 반환
     */
    @GetMapping("/next")
    public ResponseEntity<?> claimNextJob() {
        Optional<WorkerJobResponse> job = jobService.claimNextJob();
        if (job.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204: 처리할 작업 없음
        }
        return ResponseEntity.ok(job.get());
    }

    /**
     * PATCH /api/internal/jobs/{tryonId}/status
     * Python 워커가 추론 진행 상황 및 결과를 Spring에 보고
     *
     * 요청 예시 (processing):
     * { "status": "processing", "progress": 65 }
     *
     * 요청 예시 (completed):
     * { "status": "completed", "progress": 100, "result_id": "res_001" }
     *
     * 요청 예시 (failed):
     * { "status": "failed", "progress": 0,
     *   "error_code": "INFERENCE_FAILED",
     *   "error_message": "Model inference failed due to invalid input." }
     */
    @PatchMapping("/{tryonId}/status")
    public ResponseEntity<TryonResponse> updateStatus(
            @PathVariable String tryonId,
            @RequestBody WorkerStatusUpdateRequest request
    ) {
        TryonResponse response = tryonService.updateStatus(
                tryonId,
                request.getStatus(),
                request.getProgress(),
                request.getResultId(),
                request.getErrorCode(),
                request.getErrorMessage()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/internal/jobs/health
     * Python 워커가 Spring 연결 확인용으로 사용
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}