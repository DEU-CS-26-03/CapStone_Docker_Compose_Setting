//JPA CRUD + 카테고리 필터
package com.capstone.garment.repository;

import com.capstone.garment.entity.Garment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GarmentRepository extends JpaRepository<Garment, String> {

    // 카테고리 필터 (페이징 없는 단순 조회)
    List<Garment> findByCategory(String category);

    // 카테고리 필터 + 페이징 (추후 확장용)
    Page<Garment> findByCategory(String category, Pageable pageable);
}