package com.capstone.system.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class SystemController {

    private final RestTemplate restTemplate;

    @Value("${python.inference.base-url}")
    private String pythonInferenceBaseUrl;

    public SystemController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "service", "virtual-tryon-api",
                "time", OffsetDateTime.now().toString()
        ));
    }

    @GetMapping({"/models/status", "/model/health"})
    public ResponseEntity<?> modelStatus() {
        try {

            String url = pythonInferenceBaseUrl + "/v1/models/status";
            Map<?, ?> response = restTemplate.getForObject(url, Map.class);

            if (response == null) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                        "model_name", "CatVTON",
                        "loaded", false,
                        "device", "unknown",
                        "busy", false,
                        "error", "추론 서버 응답이 비어 있습니다."
                ));
            }

            Object loadedValue = response.get("loaded");
            boolean loaded = loadedValue instanceof Boolean && (Boolean) loadedValue;

            return ResponseEntity
                    .status(loaded ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE)
                    .body(response);

        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "model_name", "CatVTON",
                    "loaded", false,
                    "device", "unknown",
                    "busy", false,
                    "error", "추론 서버에 연결할 수 없습니다."
            ));
        }
    }
}