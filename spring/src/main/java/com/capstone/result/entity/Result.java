package com.capstone.result.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.OffsetDateTime;

@Getter
@Setter // ★ 에러 로그의 'setDeleted', 'getGarmentCategory' 등 해결
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "results")
public class Result {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(name = "generation_ms")
    private Integer generationMs;

    @Column(name = "garment_category")
    private String garmentCategory; // ★ getGarmentCategory() 에러 해결

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