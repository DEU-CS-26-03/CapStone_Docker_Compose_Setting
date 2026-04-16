package com.capstone.garment.repository;

import com.capstone.garment.entity.Garment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GarmentRepository extends JpaRepository<Garment, String> {

    List<Garment> findByCategory(String category);
    Page<Garment> findByCategory(String category, Pageable pageable);

    // 29CM import 중복 체크
    Optional<Garment> findByExternalItemKey(String externalItemKey);

    // 즐겨찾기 추가 전 존재 여부 확인 (existsById는 JpaRepository 기본 제공)

    // 추천 — SIMILAR: 같은 카테고리 최신 10개
    List<Garment> findTop10ByCategoryAndStatusOrderByCreatedAtDesc(
            String category, String status);

    // 추천 — CONTRAST: 다른 카테고리 최신 10개
    List<Garment> findTop10ByStatusAndCategoryNotOrderByCreatedAtDesc(
            String status, String category);

    // 추천 — MIXED: 전체 최신 10개
    List<Garment> findTop10ByStatusOrderByCreatedAtDesc(String status);
}