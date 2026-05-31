package com.capstone.result.repository;

import com.capstone.result.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResultRepository extends JpaRepository<Result, Long> {
    List<Result> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(Long userId);
    Optional<Result> findByResultId(String resultId);
    Optional<Result> findByTryonId(String tryonId);
}