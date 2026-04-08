package com.capstone.result.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public class ResultResponse {

    @JsonProperty("result_id")
    private String resultId;

    @JsonProperty("tryon_id")
    private String tryonId;

    private String status;

    @JsonProperty("result_url")
    private String resultUrl;

    @JsonProperty("thumbnail_url")
    private String thumbnailUrl;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    public ResultResponse() {}

    public ResultResponse(String resultId, String tryonId, String status,
                          String resultUrl, String thumbnailUrl, OffsetDateTime createdAt) {
        this.resultId = resultId;
        this.tryonId = tryonId;
        this.status = status;
        this.resultUrl = resultUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.createdAt = createdAt;
    }

    public String getResultId() { return resultId; }
    public String getTryonId() { return tryonId; }
    public String getStatus() { return status; }
    public String getResultUrl() { return resultUrl; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}