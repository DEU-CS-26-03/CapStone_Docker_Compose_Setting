package com.capstone.result.controller;

import com.capstone.result.dto.ResultResponse;
import com.capstone.result.service.ResultService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/results")
public class ResultController {

    private final ResultService service;

    public ResultController(ResultService service) {
        this.service = service;
    }

    /**
     * GET /api/v1/results/{id}
     * 피팅 결과 메타정보 조회 (이미지는 file_url로 직접 접근)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResultResponse> getById(@PathVariable("id") String resultId) {
        return ResponseEntity.ok(service.getById(resultId));
    }
}