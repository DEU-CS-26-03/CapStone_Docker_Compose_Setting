package com.capstone.tryon.controller;

import com.capstone.tryon.dto.TryonCreateRequest;
import com.capstone.tryon.dto.TryonResponse;
import com.capstone.tryon.service.TryonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/tryons")
public class TryonController {

    private final TryonService service;

    public TryonController(TryonService service) {
        this.service = service;
    }

    /**
     * POST /api/v1/tryons
     * 가상 피팅 작업 생성 (동시 1개만 허용 - 409 Conflict)
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody TryonCreateRequest request) {
        try {
            return ResponseEntity.ok(service.create(request));
        } catch (IllegalStateException e) {
            if ("ALREADY_ACTIVE".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of(
                                "error", "CONFLICT",
                                "message", "이미 처리 중인 작업이 있습니다. 잠시 후 다시 시도해주세요."
                        ));
            }
            throw e;
        }
    }

    /**
     * GET /api/v1/tryons/{id}
     * 작업 상태 조회 (프론트에서 2~3초 polling)
     */
    @GetMapping("/{id}")
    public ResponseEntity<TryonResponse> getById(@PathVariable("id") String tryonId) {
        return ResponseEntity.ok(service.getById(tryonId));
    }
}