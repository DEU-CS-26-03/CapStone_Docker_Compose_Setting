package com.capstone.storage.repository;

import com.capstone.storage.entity.UploadSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadSessionRepository extends JpaRepository<UploadSession, String> {
}