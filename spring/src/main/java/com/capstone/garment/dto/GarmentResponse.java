//API 응답 포맷
package com.capstone.garment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public class GarmentResponse {

    @JsonProperty("garment_id")
    private String garmentId;

    private String status;
    private String category;
    private String filename;

    @JsonProperty("content_type")
    private String contentType;

    @JsonProperty("file_url")
    private String fileUrl;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    public GarmentResponse() {}

    public GarmentResponse(String garmentId, String status, String category,
                           String filename, String contentType,
                           String fileUrl, OffsetDateTime createdAt) {
        this.garmentId = garmentId;
        this.status = status;
        this.category = category;
        this.filename = filename;
        this.contentType = contentType;
        this.fileUrl = fileUrl;
        this.createdAt = createdAt;
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