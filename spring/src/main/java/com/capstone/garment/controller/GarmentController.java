//POST /garments, GET /garments, GET /garments/{id}
package com.capstone.garment.controller;

import com.capstone.garment.dto.GarmentResponse;
import com.capstone.garment.service.GarmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/garments")
public class GarmentController {

    private final GarmentService service;

    public GarmentController(GarmentService service) {
        this.service = service;
    }

    /**
     * POST /api/v1/garments
     * 의류 이미지 업로드
     * - Content-Type: multipart/form-data
     * - file: 이미지 파일 (jpg, png만 허용)
     * - category: top / bottom / dress / outer / shoes / bag (선택)
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<GarmentResponse> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "category", required = false) String category
    ) throws IOException {
        return ResponseEntity.ok(service.upload(file, category));
    }

    /**
     * GET /api/v1/garments?category=top
     * 의류 목록 조회
     * - category 파라미터 있으면 필터링, 없으면 전체 조회
     */
    @GetMapping
    public ResponseEntity<List<GarmentResponse>> list(
            @RequestParam(value = "category", required = false) String category
    ) {
        return ResponseEntity.ok(service.list(category));
    }

    /**
     * GET /api/v1/garments/{id}
     * 의류 메타정보 단건 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<GarmentResponse> getById(
            @PathVariable("id") String garmentId
    ) {
        return ResponseEntity.ok(service.getById(garmentId));
    }
}