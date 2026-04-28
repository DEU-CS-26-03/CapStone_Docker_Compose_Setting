package com.capstone.garment.repository;

import com.capstone.garment.entity.Garment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GarmentRepository extends JpaRepository<Garment, String> { // Long → String

    // ── 상품 검색 (CatalogService, GarmentService 공용) ─────────
    @Query("""
        SELECT g FROM Garment g
        WHERE g.status NOT IN ('DELETED', 'HIDDEN')
          AND (:category IS NULL OR :category = '' OR g.category = :category)
          AND (:sourceType IS NULL OR :sourceType = '' OR g.sourceType = :sourceType)
          AND (:brandKey IS NULL OR :brandKey = '' OR g.brandKey = :brandKey)
          AND (:q IS NULL OR :q = ''
               OR LOWER(g.name) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(g.filename) LIKE LOWER(CONCAT('%', :q, '%')))
        ORDER BY g.createdAt DESC
    """)
    List<Garment> searchGarments(
            @Param("q") String q,
            @Param("category") String category,
            @Param("sourceType") String sourceType,
            @Param("brandKey") String brandKey
    );

    // ── 외부 상품키로 조회 (importItem 중복 체크용) ───────────────
    Optional<Garment> findByExternalItemKey(String externalItemKey);

    // ── ResultService용 쿼리 메서드 ──────────────────────────────
    List<Garment> findTop10ByCategoryAndStatusOrderByCreatedAtDesc(String category, String status);

    List<Garment> findTop10ByStatusAndCategoryNotOrderByCreatedAtDesc(String status, String category);

    List<Garment> findTop10ByStatusOrderByCreatedAtDesc(String status);
}