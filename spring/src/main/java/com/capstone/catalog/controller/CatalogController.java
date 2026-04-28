package com.capstone.catalog.controller;

import com.capstone.catalog.dto.*;
import com.capstone.catalog.service.CatalogService;
import com.capstone.garment.dto.GarmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    // GET /api/v1/catalog/categories/standard
    @GetMapping("/categories/standard")
    public ResponseEntity<List<CategoryResponse>> getStandardCategories() {
        return ResponseEntity.ok(catalogService.getStandardCategories());
    }

    // GET /api/v1/catalog/categories/display
    @GetMapping("/categories/display")
    public ResponseEntity<List<CategoryResponse>> getDisplayCategories() {
        return ResponseEntity.ok(catalogService.getDisplayCategories());
    }

    // GET /api/v1/catalog/categories/{code}
    @GetMapping("/categories/{code}")
    public ResponseEntity<CategoryResponse> getCategoryByCode(@PathVariable String code) {
        return ResponseEntity.ok(catalogService.getCategoryByCode(code));
    }

    // GET /api/v1/catalog/brands
    @GetMapping("/brands")
    public ResponseEntity<List<BrandResponse>> getBrands() {
        return ResponseEntity.ok(catalogService.getBrands());
    }

    // GET /api/v1/catalog/brands/{brandKey}
    @GetMapping("/brands/{brandKey}")
    public ResponseEntity<BrandResponse> getBrandByKey(@PathVariable String brandKey) {
        return ResponseEntity.ok(catalogService.getBrandByKey(brandKey));
    }

    // GET /api/v1/catalog/items — 상품 목록 검색 (POST→GET으로 변경, query param 방식)
    @GetMapping("/items")
    public ResponseEntity<List<GarmentResponse>> searchItems(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brandKey,
            @RequestParam(required = false) String sourceType
    ) {
        return ResponseEntity.ok(catalogService.searchItems(q, category, brandKey, sourceType));
    }

    // GET /api/v1/catalog/items/{itemId}
    @GetMapping("/items/{itemId}")
    public ResponseEntity<GarmentResponse> getItem(@PathVariable String itemId) {
        return ResponseEntity.ok(catalogService.getItemById(itemId));
    }

    // POST /api/v1/catalog/import — ADMIN 전용, 외부 상품을 garment DB에 저장
    @PostMapping("/import")
    public ResponseEntity<GarmentResponse> importItem(
            @RequestBody Map<String, String> body
    ) {
        return ResponseEntity.ok(catalogService.importItem(body.get("itemKey")));
    }
}