package com.capstone.web_api.cm29.service;

import com.capstone.web_api.naver.client.NaverShoppingApiClient;
import com.capstone.web_api.cm29.dto.*;
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
public class Cm29Service {

    private final GarmentRepository garmentRepository;
    private final NaverShoppingApiClient naverApiClient;  // ← 주입 추가

    // ── 표준 카테고리 (Mock 유지 — 네이버에 카테고리 API 없음) ──

    public List<Cm29CategoryResponse> getStandardCategories() {
        return List.of(
                new Cm29CategoryResponse("TOP", "상의", 1, null, false, false, false,
                        List.of(
                                new Cm29CategoryResponse("TOP_TSHIRT", "티셔츠", 2, "TOP", true, true, true, null),
                                new Cm29CategoryResponse("TOP_SHIRT",  "셔츠",   2, "TOP", true, true, true, null),
                                new Cm29CategoryResponse("TOP_KNIT",   "니트",   2, "TOP", true, true, true, null)
                        )),
                new Cm29CategoryResponse("BOTTOM", "하의", 1, null, false, false, false,
                        List.of(
                                new Cm29CategoryResponse("BOTTOM_PANTS",  "팬츠/슬랙스", 2, "BOTTOM", true, true, true, null),
                                new Cm29CategoryResponse("BOTTOM_JEANS",  "청바지",      2, "BOTTOM", true, true, true, null),
                                new Cm29CategoryResponse("BOTTOM_SHORTS", "반바지",      2, "BOTTOM", true, true, true, null)
                        )),
                new Cm29CategoryResponse("OUTER", "아우터", 1, null, false, false, false,
                        List.of(
                                new Cm29CategoryResponse("OUTER_JACKET", "자켓", 2, "OUTER", true, true, true, null),
                                new Cm29CategoryResponse("OUTER_COAT",   "코트", 2, "OUTER", true, true, true, null),
                                new Cm29CategoryResponse("OUTER_JUMPER", "점퍼", 2, "OUTER", true, true, true, null)
                        )),
                new Cm29CategoryResponse("DRESS", "원피스/스커트", 1, null, false, false, false,
                        List.of(
                                new Cm29CategoryResponse("DRESS_ONE",   "원피스", 2, "DRESS", true, true, true, null),
                                new Cm29CategoryResponse("DRESS_SKIRT", "스커트", 2, "DRESS", true, true, true, null)
                        ))
        );
    }

    // ── 전시 카테고리 (Mock 유지) ────────────────────────────────

    public List<Cm29CategoryResponse> getDisplayCategories() {
        return List.of(
                new Cm29CategoryResponse("DISPLAY_MEN",    "남성",    1, null, false, false, false, null),
                new Cm29CategoryResponse("DISPLAY_WOMEN",  "여성",    1, null, false, false, false, null),
                new Cm29CategoryResponse("DISPLAY_UNISEX", "유니섹스",1, null, false, false, false, null)
        );
    }

    // ── 카테고리 상세 ────────────────────────────────────────────

    public Cm29CategoryResponse getCategoryByCode(String code) {
        return getStandardCategories().stream()
                .flatMap(cat -> cat.getChildren() == null
                        ? Stream.of(cat)
                        : Stream.concat(Stream.of(cat), cat.getChildren().stream()))
                .filter(cat -> cat.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + code));
    }

    // ── 브랜드 (Mock 유지 — 네이버에 브랜드 목록 API 없음) ──────

    public List<Cm29BrandResponse> getBrands() {
        return List.of(
                new Cm29BrandResponse("brand_nike",    "Nike",        "나이키",          "https://picsum.photos/seed/nike/100/100",    "ACTIVE"),
                new Cm29BrandResponse("brand_adidas",  "Adidas",      "아디다스",        "https://picsum.photos/seed/adidas/100/100",  "ACTIVE"),
                new Cm29BrandResponse("brand_uniqlo",  "UNIQLO",      "유니클로",        "https://picsum.photos/seed/uniqlo/100/100",  "ACTIVE"),
                new Cm29BrandResponse("brand_zara",    "ZARA",        "자라",            "https://picsum.photos/seed/zara/100/100",    "ACTIVE"),
                new Cm29BrandResponse("brand_musinsa", "Musinsa Std", "무신사 스탠다드", "https://picsum.photos/seed/musinsa/100/100", "ACTIVE")
        );
    }

    public Cm29BrandResponse getBrandByKey(String partnerBrandKey) {
        return getBrands().stream()
                .filter(b -> b.getPartnerBrandKey().equals(partnerBrandKey))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("브랜드를 찾을 수 없습니다: " + partnerBrandKey));
    }

    // ── 상품 검색 — 네이버 실제 API 호출 ────────────────────────

    public List<Cm29ItemResponse> searchCatalog(Cm29CatalogSearchRequest request) {
        String query   = request.getItemName() != null ? request.getItemName() : "의류";
        int    display = request.getSize()     != null ? request.getSize()     : 10;

        List<Cm29ItemResponse> results = naverApiClient.searchItems(query, display);

        // brandKey가 있으면 브랜드명으로 추가 필터링
        if (request.getBrandKey() != null) {
            String brandName = getBrands().stream()
                    .filter(b -> b.getPartnerBrandKey().equals(request.getBrandKey()))
                    .map(Cm29BrandResponse::getBrandName)
                    .findFirst()
                    .orElse(null);

            if (brandName != null) {
                final String lowerBrand = brandName.toLowerCase();
                results = results.stream()
                        .filter(item -> item.getBrandName() != null &&
                                item.getBrandName().toLowerCase().contains(lowerBrand))
                        .collect(Collectors.toList());
            }
        }

        return results;
    }

    // ── 상품 상세 — productId로 단건 조회 ───────────────────────

    public Cm29ItemResponse getCatalogItem(String itemKey) {
        // 네이버는 itemKey(productId)로 직접 단건 조회 불가
        // → 키워드 검색 후 productId 일치하는 것 반환
        return naverApiClient.searchItems(itemKey, 5).stream()
                .filter(item -> item.getItemKey().equals(itemKey))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + itemKey));
    }

    // ── 상품 import ──────────────────────────────────────────────

    @Transactional
    public GarmentResponse importItem(String itemKey) {
        garmentRepository.findByExternalItemKey(itemKey).ifPresent(existing -> {
            throw new IllegalStateException("이미 import된 상품입니다: " + itemKey);
        });

        Cm29ItemResponse item = getCatalogItem(itemKey);

        Garment garment = new Garment();
        garment.setGarmentId("gar_naver_" + itemKey);
        garment.setSourceType("29CM_IMPORT");
        garment.setExternalItemKey(item.getItemKey());
        garment.setPartnerBrandKey(item.getPartnerBrandKey());
        garment.setBrandName(item.getBrandName());
        garment.setStandardCategoryCode(item.getStandardCategoryCode());
        garment.setName(item.getItemName());
        garment.setFileUrl(item.getThumbnailUrl());
        garment.setThumbnailUrl(item.getThumbnailUrl());
        garment.setPrice(item.getSalePrice());
        garment.setCurrency(item.getCurrency() != null ? item.getCurrency() : "KRW");
        garment.setStatus("ACTIVE");

        Garment saved = garmentRepository.save(garment);

        return new GarmentResponse(
                saved.getGarmentId(),
                saved.getStatus(),
                saved.getSourceType(),
                saved.getCategory(),
                saved.getName(),
                null,
                saved.getFileUrl(),
                saved.getPartnerBrandKey(),
                saved.getCreatedAt()
        );
    }

    // ── Presign URL (Mock 유지) ──────────────────────────────────

    public String generatePresignedUrl(String filename) {
        String mockKey = "mock_" + System.currentTimeMillis() + "_" + filename;
        return "/api/v1/user-images/upload?key=" + mockKey;
    }
}