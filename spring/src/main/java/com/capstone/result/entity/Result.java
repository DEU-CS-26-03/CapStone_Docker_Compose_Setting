package com.capstone.result.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tryon_results")
public class TryonResult {

    @Id
    @Column(name = "result_id", length = 50)
    private String resultId;

    @Column(name = "tryon_id", nullable = false)
    private String tryonId;

    @Column(nullable = false, length = 20)
    private String status = "available";

    @Column(name = "result_url")
    private String resultUrl;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = OffsetDateTime.now(); }

    public String getResultId() { return resultId; }
    public void setResultId(String resultId) { this.resultId = resultId; }
    public String getTryonId() { return tryonId; }
    public void setTryonId(String tryonId) { this.tryonId = tryonId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResultUrl() { return resultUrl; }
    public void setResultUrl(String resultUrl) { this.resultUrl = resultUrl; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}