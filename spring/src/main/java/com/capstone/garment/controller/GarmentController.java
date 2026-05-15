package com.capstone.garment.controller;

import com.capstone.garment.dto.GarmentResponse;
import com.capstone.garment.dto.GarmentUpdateRequest;
import com.capstone.garment.service.GarmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/garments")
@RequiredArgsConstructor
public class GarmentController {

    private final GarmentService service;

    // POST /api/v1/garments
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<GarmentResponse> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "category",  required = false) String category,
            @RequestParam(value = "name",      required = false) String name,      // 추가
            @RequestParam(value = "brandName", required = false) String brandName, // 추가
            @RequestParam(value = "price",     required = false) String price,     // 추가
            Authentication authentication
    ) throws IOException {
        String email = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.upload(file, category, name, brandName, price, email));
    }

    @DeleteMapping("/{garmentId}")
    @PreAuthorize("hasRole('ADMIN')") // ★ 관리자만 삭제 가능하도록 보안 강화
    public ResponseEntity<Map<String, String>> delete(@PathVariable String garmentId) {
        service.softDelete(garmentId);
        return ResponseEntity.ok(Map.of("message", "의류가 비노출 처리되었습니다."));
    }

    // GET /api/v1/garments?q=&category=&sourceType=&brandKey=
    @GetMapping
    public ResponseEntity<List<GarmentResponse>> list(
            @RequestParam(value = "q",          required = false) String q,
            @RequestParam(value = "category",   required = false) String category,
            @RequestParam(value = "sourceType", required = false) String sourceType,
            @RequestParam(value = "brandKey",   required = false) String brandKey
    ) {
        return ResponseEntity.ok(service.list(q, category, sourceType, brandKey));
    }

    // GET /api/v1/garments/{garmentId}
    @GetMapping("/{garmentId}")
    public ResponseEntity<GarmentResponse> getById(@PathVariable String garmentId) {
        return ResponseEntity.ok(service.getById(garmentId));
    }

    // PATCH /api/v1/garments/{garmentId}
    @PatchMapping("/{garmentId}")
    public ResponseEntity<GarmentResponse> update(
            @PathVariable String garmentId,
            @RequestBody GarmentUpdateRequest request
    ) {
        return ResponseEntity.ok(service.update(garmentId, request));
    }

    // ★ 추가됨: 가상 피팅 결과 기반 맞춤형 의류 추천 (별점 연동)
    // GET /api/v1/garments/recommend?type=similar&category=upper
    @GetMapping("/recommend")
    public ResponseEntity<List<GarmentResponse>> recommend(
            @RequestParam(value = "type", defaultValue = "similar") String type,
            @RequestParam(value = "category", defaultValue = "upper") String category
    ) {
        return ResponseEntity.ok(service.getRecommendations(type, category));
    }
}