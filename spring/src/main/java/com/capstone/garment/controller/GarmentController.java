package com.capstone.garment.controller;

import com.capstone.garment.dto.GarmentResponse;
import com.capstone.garment.dto.GarmentUpdateRequest;
import com.capstone.garment.service.GarmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    // 💡 수정됨: 파일과 URL 모두 유연하게 받을 수 있도록 consumes 및 required 옵션 수정
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GarmentResponse> upload(
            @RequestParam(value = "file",      required = false) MultipartFile file,     // 파일 필수 해제
            @RequestParam(value = "fileUrl",   required = false) String fileUrl,         // ★ 추가: 웹 URL
            @RequestParam(value = "category",  required = false) String category,
            @RequestParam(value = "name",      required = false) String name,
            @RequestParam(value = "brandName", required = false) String brandName,
            @RequestParam(value = "price",     required = false) String price,
            Authentication authentication
    ) throws IOException {
        String email = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                // ★ 파라미터에 fileUrl 추가
                .body(service.upload(file, fileUrl, category, name, brandName, price, email));
    }

    @DeleteMapping("/{garmentId}")
    @PreAuthorize("hasRole('ADMIN')")
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

    // GET /api/v1/garments/recommend?type=similar&category=upper
    @GetMapping("/recommend")
    public ResponseEntity<List<GarmentResponse>> recommend(
            @RequestParam(value = "type", defaultValue = "similar") String type,
            @RequestParam(value = "category", defaultValue = "upper") String category
    ) {
        return ResponseEntity.ok(service.getRecommendations(type, category));
    }
}