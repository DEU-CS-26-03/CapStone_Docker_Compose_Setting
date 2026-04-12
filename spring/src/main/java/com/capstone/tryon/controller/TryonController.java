package com.capstone.tryon.controller;

import com.capstone.tryon.dto.TryonCreateRequest;
import com.capstone.tryon.dto.TryonResponse;
import com.capstone.tryon.service.TryonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/api/v1/tryons", "/api/v1/tryon/jobs"})
@RequiredArgsConstructor
public class TryonController {

    private final TryonService tryonService;

    @PostMapping
    public ResponseEntity<?> createJob(@RequestBody TryonCreateRequest request) {
        return ResponseEntity.ok(tryonService.createJob(request));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<?> getJob(@PathVariable String jobId) {
        return ResponseEntity.ok(tryonService.getJob(jobId));
    }

    @GetMapping("/{jobId}/result")
    public ResponseEntity<?> getResult(@PathVariable String jobId) {
        TryonResponse job = tryonService.getJob(jobId);

        if (job.getResultId() == null || job.getResultId().isBlank()) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                    "tryonId", jobId,
                    "status", job.getStatus(),
                    "message", "아직 결과가 생성되지 않았습니다."
            ));
        }

        return ResponseEntity.ok(Map.of(
                "tryonId", jobId,
                "resultId", job.getResultId(),
                "status", job.getStatus()
        ));
    }
}