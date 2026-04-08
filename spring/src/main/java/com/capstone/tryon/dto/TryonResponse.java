
package com.capstone.tryon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class TryonResponse {

    @JsonProperty("tryon_id")
    private String tryonId;

    private String status;
    private int progress;
    private String message;

    @JsonProperty("user_image_id")
    private String userImageId;

    @JsonProperty("garment_id")
    private String garmentId;

    @JsonProperty("result_id")
    private String resultId;

    private TryonErrorInfo error;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;

    // Getters & Setters (전부)
    public String getTryonId() { return tryonId; }
    public void setTryonId(String tryonId) { this.tryonId = tryonId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getUserImageId() { return userImageId; }
    public void setUserImageId(String userImageId) { this.userImageId = userImageId; }
    public String getGarmentId() { return garmentId; }
    public void setGarmentId(String garmentId) { this.garmentId = garmentId; }
    public String getResultId() { return resultId; }
    public void setResultId(String resultId) { this.resultId = resultId; }
    public TryonErrorInfo getError() { return error; }
    public void setError(TryonErrorInfo error) { this.error = error; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}