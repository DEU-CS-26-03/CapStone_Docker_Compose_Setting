package com.capstone.tryon.repository;

import com.capstone.tryon.entity.TryonJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TryonJobRepository extends JpaRepository<TryonJob, String> {

    // 사용자별 목록 조회 (soft delete 제외)
    List<TryonJob> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(Long userId);

    // ✅ worker가 다음 작업 가져올 때 사용
    @Query("SELECT j FROM TryonJob j WHERE j.status IN :statuses AND j.deleted = false ORDER BY j.createdAt ASC")
    Optional<TryonJob> findFirstByStatusIn(@Param("statuses") List<String> statuses);

    // tryonId + userId 조합으로 단건 조회
    Optional<TryonJob> findByTryonIdAndDeletedFalse(String tryonId);

    @Query("SELECT j FROM TryonJob j WHERE j.status IN :statuses AND j.deleted = false ORDER BY j.createdAt ASC")
    Optional<TryonJob> findFirstByStatusInOrderByCreatedAtAsc(@Param("statuses") List<String> statuses);

    Optional<TryonJob> findFirstByStatusOrderByCreatedAtAsc(String status);

    // 특정 상태 건수 확인 (모니터링용)
    long countByStatus(String status);
}