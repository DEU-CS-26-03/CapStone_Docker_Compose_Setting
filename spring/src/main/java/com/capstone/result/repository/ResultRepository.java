package com.capstone.result.repository;

import com.capstone.result.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResultRepository extends JpaRepository<Result, String> {

    // 내 결과 목록 조회 (soft delete 제외, 최신순)
    List<Result> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(Long userId);

    // tryonId로 결과 조회 (InternalJobController에서 결과 저장 시 사용)
    Optional<Result> findByTryonId(String tryonId);
}