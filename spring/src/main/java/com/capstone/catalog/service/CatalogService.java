package com.capstone.catalog.service;

import com.capstone.catalog.dto.*;
import com.capstone.garment.dto.GarmentResponse;
import com.capstone.garment.entity.Garment;
import com.capstone.garment.repository.GarmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final GarmentRepository garmentRepository;
    // NaverShoppingApiClient 제거 — catalog/provider가 담당

    // ── 표준 카테고리 ────────────────────────────────────────────
    public List<CategoryResponse> getStandardCategories() {
        return List.of(
                new CategoryResponse("TOP", "상의", 1, null,
                        List.of(
                                new CategoryResponse("TOP_TSHIRT", "티셔츠", 2, "TOP", null),
                                new CategoryResponse("TOP_SHIRT",  "셔츠",   2, "TOP", null),
                                new CategoryResponse("TOP_KNIT",   "니트",   2, "TOP", null)
                        )),
                new CategoryResponse("BOTTOM", "하의", 1, null,
                        List.of(
                                new CategoryResponse("BOTTOM_PANTS",  "팬츠/슬랙스", 2, "BOTTOM", null),
                                new CategoryResponse("BOTTOM_JEANS",  "청바지",      2, "BOTTOM", null),
                                new CategoryResponse("BOTTOM_SHORTS", "반바지",      2, "BOTTOM", null)
                        )),
                new CategoryResponse("OUTER", "아우터", 1, null,
                        List.of(
                                new CategoryResponse("OUTER_JACKET", "자켓", 2, "OUTER", null),
                                new CategoryResponse("OUTER_COAT",   "코트", 2, "OUTER", null),
                                new CategoryResponse("OUTER_JUMPER", "점퍼", 2, "OUTER", null)
                        )),
                new CategoryResponse("DRESS", "원피스/스커트", 1, null,
                        List.of(
                                new CategoryResponse("DRESS_ONE",   "원피스", 2, "DRESS", null),
                                new CategoryResponse("DRESS_SKIRT", "스커트", 2, "DRESS", null)
                        ))
        );
    }

    // ── 전시 카테고리 ────────────────────────────────────────────
    public List<CategoryResponse> getDisplayCategories() {
        return List.of(
                new CategoryResponse("DISPLAY_MEN",    "남성",     1, null, null),
                new CategoryResponse("DISPLAY_WOMEN",  "여성",     1, null, null),
                new CategoryResponse("DISPLAY_UNISEX", "유니섹스", 1, null, null)
        );
    }

    // ── 카테고리 상세 ────────────────────────────────────────────
    public CategoryResponse getCategoryByCode(String code) {
        return getStandardCategories().stream()
                .flatMap(cat -> cat.getChildren() == null
                        ? Stream.of(cat)
                        : Stream.concat(Stream.of(cat), cat.getChildren().stream()))
                .filter(cat -> cat.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("카테고리 없음: " + code));
    }

    // ── 브랜드 (DB 없으면 Mock, 나중에 Brand 엔티티 추가 권장) ──
    public List<BrandResponse> getBrands() {
        return List.of(
                new BrandResponse("brand_nike",    "Nike",        "나이키"),
                new BrandResponse("brand_adidas",  "Adidas",      "아디다스"),
                new BrandResponse("brand_uniqlo",  "UNIQLO",      "유니클로"),
                new BrandResponse("brand_zara",    "ZARA",        "자라"),
                new BrandResponse("brand_musinsa", "Musinsa Std", "무신사 스탠다드")
        );
    }

    public BrandResponse getBrandByKey(String brandKey) {
        return getBrands().stream()
                .filter(b -> b.getBrandKey().equals(brandKey))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("브랜드 없음: " + brandKey));
    }

    // ── DB 기반 상품 검색 (GarmentService.list()와 동일 로직 재사용) ──
    public List<GarmentResponse> searchItems(
            String q, String category, String brandKey, String sourceType) {
        return garmentRepository.findAll().stream()
                .filter(g -> !"DELETED".equals(g.getStatus()) && !"HIDDEN".equals(g.getStatus()))
                .filter(g -> category   == null || category.isBlank()
                        || category.equalsIgnoreCase(g.getCategory()))
                .filter(g -> brandKey   == null || brandKey.isBlank()
                        || brandKey.equals(g.getBrandKey()))
                .filter(g -> sourceType == null || sourceType.isBlank()
                        || sourceType.equals(g.getSourceType()))
                .filter(g -> q == null || q.isBlank()
                        || (g.getName() != null && g.getName().toLowerCase().contains(q.toLowerCase()))
                        || (g.getFilename() != null && g.getFilename().toLowerCase().contains(q.toLowerCase())))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── 상품 단건 조회 ───────────────────────────────────────────
    public GarmentResponse getItemById(String itemId) {
        return garmentRepository.findById(itemId)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("상품 없음: " + itemId));
    }

    // ── 외부 데이터 import → garment DB 저장 (ADMIN 전용) ────────
    @Transactional
    public GarmentResponse importItem(String itemKey) {
        if (itemKey == null || itemKey.isBlank()) {
            throw new IllegalArgumentException("itemKey는 필수입니다.");
        }
        garmentRepository.findByExternalItemKey(itemKey).ifPresent(existing -> {
            throw new IllegalStateException("이미 import된 상품: " + itemKey);
        });

        // 현재는 itemKey로 수동 등록 — 추후 Naver API 연결 시 여기서 호출
        Garment garment = new Garment();
        garment.setGarmentId("gar_import_" + itemKey);
        garment.setSourceType("IMPORT");
        garment.setExternalItemKey(itemKey);
        garment.setStatus("ACTIVE");

        Garment saved = garmentRepository.save(garment);
        return toResponse(saved);
    }

    private GarmentResponse toResponse(Garment g) {
        return new GarmentResponse(
                g.getGarmentId(),
                g.getStatus(),
                g.getSourceType(),
                g.getCategory(),
                g.getName() != null ? g.getName() : g.getFilename(),
                g.getContentType(),
                g.getFileUrl(),
                g.getBrandKey(),
                g.getCreatedAt()
        );
    }
}