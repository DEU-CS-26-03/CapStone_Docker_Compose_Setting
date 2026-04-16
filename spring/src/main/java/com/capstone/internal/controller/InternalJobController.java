package com.capstone.internal.controller;

import com.capstone.internal.dto.InternalJobResponse;
import com.capstone.internal.dto.InternalJobStatusRequest;
import com.capstone.tryon.dto.TryonResponse;
import com.capstone.tryon.entity.TryonJob;
import com.capstone.tryon.repository.TryonJobRepository;
import com.capstone.tryon.service.TryonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Python Worker 전용 내부 API
 * - 외부에서 접근 불가하도록 SecurityConfig에서 /api/internal/** 은
 *   IP 제한 또는 Worker-Token 헤더 검증으로 보호하는 것을 권장
 */
@RestController
@RequestMapping("/api/internal/jobs")
@RequiredArgsConstructor
public class InternalJobController {

    private final TryonService tryonService;
    private final TryonJobRepository tryonJobRepository;

    /**
     * GET /api/internal/jobs/next
     * Python worker가 다음 처리할 작업을 가져가는 엔드포인트
     */
    @GetMapping("/next")
    public ResponseEntity<?> claimNextJob() {
        Optional<TryonResponse> job = tryonService.claimNextPendingJob();

        if (job.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        TryonResponse tryon = job.get();

        // 실제 파일 경로는 DB에서 조회 (userImageId → fileUrl, garmentId → fileUrl)
        TryonJob entity = tryonJobRepository.findById(tryon.getTryonId()).orElseThrow();
        // userImagePath, garmentPath는 각 서비스에서 조회하도록 확장 가능
        InternalJobResponse response = new InternalJobResponse(
                tryon.getTryonId(),
                "/files/user-images/" + entity.getUserImageId(),
                "/files/garments/"   + entity.getGarmentId(),
                tryon.getStatus()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/internal/jobs/{tryonId}/status
     * Python worker가 처리 상태와 결과를 보고하는 엔드포인트
     */
    @PatchMapping("/{tryonId}/status")
    public ResponseEntity<TryonResponse> updateStatus(
            @PathVariable String tryonId,
            @RequestBody InternalJobStatusRequest request
    ) {
        TryonResponse updated = tryonService.updateStatus(
                tryonId,
                request.getStatus(),
                request.getProgress(),
                request.getResultId(),
                request.getErrorCode(),
                request.getErrorMessage()
        );
        return ResponseEntity.ok(updated);
    }

    /**
     * GET /api/internal/jobs/health
     * Python worker가 백엔드 연결 상태를 확인하는 헬스체크
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "message", "Internal job API is running"
        ));
    }
}