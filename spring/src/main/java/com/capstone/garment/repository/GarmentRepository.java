package com.capstone.garment.repository;

import com.capstone.garment.entity.Garment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GarmentRepository extends JpaRepository<Garment, String> { // ID 타입 맞게 조정

    // ResultService에서 필요
    List<Garment> findTop10ByCategoryAndStatusOrderByCreatedAtDesc(String category, String status);
    List<Garment> findTop10ByStatusAndCategoryNotOrderByCreatedAtDesc(String status, String category);
    List<Garment> findTop10ByStatusOrderByCreatedAtDesc(String status);

    // CatalogService에서 필요
    Optional<Garment> findByExternalItemKey(String externalItemKey);

    @Query("""
        SELECT g FROM Garment g
        WHERE g.status NOT IN ('DELETED', 'HIDDEN')
          AND (:category IS NULL OR :category = '' OR g.category = :category)
          AND (:sourceType IS NULL OR :sourceType = '' OR g.sourceType = :sourceType)
          AND (:brandKey IS NULL OR :brandKey = '' OR g.brandKey = :brandKey)
          AND (:q IS NULL OR :q = '' OR LOWER(g.filename) LIKE LOWER(CONCAT('%', :q, '%')))
        ORDER BY g.createdAt DESC
    """)
    List<Garment> searchGarments(
            @Param("q") String q,
            @Param("category") String category,
            @Param("sourceType") String sourceType,
            @Param("brandKey") String brandKey
    );
}