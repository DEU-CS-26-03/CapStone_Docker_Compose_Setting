// src/main/java/com/capstone/tryon/entity/TryonJob.java
package com.capstone.tryon.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "tryon_jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TryonJob {

    @Id
    @Column(name = "tryon_id")
    private String tryonId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_image_id")
    private String userImageId;

    @Column(name = "garment_id")
    private String garmentId;

    @Column(name = "external_item_key")
    private String externalItemKey;

    @Column(name = "status")
    private String status;

    @Column(name = "progress")
    private int progress;

    @Column(name = "result_id")
    private String resultId;

    @Column(name = "result_image_url")       // ← 추가: Python 결과 이미지 URL 저장
    private String resultImageUrl;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "deleted")
    @Builder.Default
    private boolean deleted = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}