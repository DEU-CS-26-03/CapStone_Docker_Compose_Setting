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
            @RequestParam(value = "name",      required = false) String name,      // ★ 추가: 프론트의 name 수신
            @RequestParam(value = "brandName", required = false) String brandName, // ★ 추가: 프론트의 brandName 수신
            @RequestParam(value = "price",     required = false) String price,     // ★ 추가: 프론트의 price 수신
            Authentication authentication
    ) throws IOException {
        if (authentication == null) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }

        String email = authentication.getName();

        // ★ service.upload 호출 시 파라미터 추가 전송
        // (주의: GarmentService.java의 upload 메서드 시그니처도 함께 수정되어야 합니다)
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.upload(file, category, name, brandName, price, email));
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

    // DELETE /api/v1/garments/{garmentId}
    @DeleteMapping("/{garmentId}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String garmentId) {
        service.softDelete(garmentId);
        return ResponseEntity.ok(Map.of("message", "의류가 비노출 처리되었습니다."));
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