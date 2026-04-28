package com.capstone.storage.controller;

import com.capstone.result.dto.FeedbackRequest;
import com.capstone.result.dto.FeedbackResponse;
import com.capstone.result.dto.ResultResponse;
import com.capstone.result.service.ResultService;
import com.capstone.garment.dto.GarmentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/results")
@RequiredArgsConstructor
public class StorageResultController {

    private final ResultService service;

    // GET /api/v1/results — 내 결과 목록 조회 (기존 /histories 흡수)
    @GetMapping
    public ResponseEntity<List<ResultResponse>> list(Authentication authentication) {
        return ResponseEntity.ok(service.listByUser(authentication.getName()));
    }

    // GET /api/v1/results/{resultId} — 결과 상세 조회
    @GetMapping("/{resultId}")
    public ResponseEntity<ResultResponse> getById(@PathVariable String resultId) {
        return ResponseEntity.ok(service.getById(resultId));
    }

    // DELETE /api/v1/results/{resultId} — 결과 삭제
    @DeleteMapping("/{resultId}")
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable String resultId,
            Authentication authentication
    ) {
        service.softDelete(resultId, authentication.getName());
        return ResponseEntity.ok(Map.of("message", "결과가 삭제되었습니다."));
    }

    // POST /api/v1/results/{resultId}/feedback — 별점 저장
    @PostMapping("/{resultId}/feedback")
    public ResponseEntity<FeedbackResponse> saveFeedback(
            @PathVariable String resultId,
            @Valid @RequestBody FeedbackRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                service.saveFeedback(resultId, request, authentication.getName()));
    }

    // GET /api/v1/results/{resultId}/recommendations — 별점 기반 추천 조회
    @GetMapping("/{resultId}/recommendations")
    public ResponseEntity<List<GarmentResponse>> getRecommendations(
            @PathVariable String resultId
    ) {
        return ResponseEntity.ok(service.getRecommendations(resultId));
    }
}