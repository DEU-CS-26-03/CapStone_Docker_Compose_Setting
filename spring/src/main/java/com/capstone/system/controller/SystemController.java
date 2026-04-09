// Health & Model Status
package com.capstone.system.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class SystemController {

    private final RestTemplate restTemplate;

    @Value("${python.inference.base-url}")
    private String pythonBaseUrl;

    public SystemController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * GET /api/v1/health
     * Spring 서버 생존 확인
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "service", "virtual-tryon-api",
                "time", OffsetDateTime.now().toString()
        ));
    }

    /**
     * GET /api/v1/models/status
     * Python 추론 서버의 모델 로드 상태 프록시
     */
    @GetMapping("/models/status")
    public ResponseEntity<?> modelStatus() {
        try {
            String url = pythonBaseUrl + "/v1/models/status";
            Map<?, ?> response = restTemplate.getForObject(url, Map.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(503).body(Map.of(
                    "model_name", "CatVTON",
                    "loaded", false,
                    "device", "unknown",
                    "busy", false,
                    "error", "추론 서버에 연결할 수 없습니다."
            ));
        }
    }
}