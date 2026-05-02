package com.capstone.result.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "results")
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ 외부 노출용 결과 ID (UUID 등)
    @Column(name = "result_id", unique = true)
    private String resultId;

    @Column(name = "tryon_id")
    private String tryonId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "result_image_url")
    private String resultImageUrl;

    @Column(name = "result_thumbnail_url")
    private String resultThumbnailUrl;

    @Column(name = "storage_path")
    private String storagePath;

    // ✅ AI 생성 소요 시간 (ms)
    @Column(name = "generation_ms")
    private Integer generationMs;

    // ✅ 피팅 당시 의류 카테고리 스냅샷
    @Column(name = "garment_category")
    private String garmentCategory;

    private int rating;
    private String comment;

    @Column(name = "recommendation_mode")
    private String recommendationMode;

    @Builder.Default
    private boolean deleted = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}