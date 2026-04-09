//DB 테이블 매핑 (garments)
package com.capstone.garment.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "garments")
public class Garment {

    @Id
    @Column(name = "garment_id", length = 50)
    private String garmentId;

    @Column(nullable = false, length = 20)
    private String status = "uploaded";

    @Column(length = 30)
    private String category;

    @Column(nullable = false)
    private String filename;

    @Column(name = "content_type", length = 50)
    private String contentType;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public String getGarmentId() { return garmentId; }
    public void setGarmentId(String garmentId) { this.garmentId = garmentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}