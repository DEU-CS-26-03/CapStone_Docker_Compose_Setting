package com.capstone.tryon.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tryon_jobs")
public class TryonJob {

    @Id
    @Column(name = "tryon_id", length = 50)
    private String tryonId;

    @Column(nullable = false, length = 20)
    private String status = "queued"; // queued | processing | completed | failed

    @Column(nullable = false)
    private int progress = 0;

    @Column(name = "user_image_id", nullable = false)
    private String userImageId;

    @Column(name = "garment_id", nullable = false)
    private String garmentId;

    @Column(name = "result_id")
    private String resultId;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    // Getters & Setters
    public String getTryonId() { return tryonId; }
    public void setTryonId(String tryonId) { this.tryonId = tryonId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public String getUserImageId() { return userImageId; }
    public void setUserImageId(String userImageId) { this.userImageId = userImageId; }
    public String getGarmentId() { return garmentId; }
    public void setGarmentId(String garmentId) { this.garmentId = garmentId; }
    public String getResultId() { return resultId; }
    public void setResultId(String resultId) { this.resultId = resultId; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}