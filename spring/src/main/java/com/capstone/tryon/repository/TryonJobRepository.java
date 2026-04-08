package com.capstone.tryon.repository;

import com.capstone.tryon.entity.TryonJob;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TryonJobRepository extends JpaRepository<TryonJob, String> {
    Optional<TryonJob> findFirstByStatusIn(java.util.List<String> statuses);
}