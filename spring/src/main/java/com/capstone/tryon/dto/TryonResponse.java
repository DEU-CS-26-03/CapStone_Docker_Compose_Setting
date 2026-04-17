// src/main/java/com/capstone/tryon/dto/TryonResponse.java
package com.capstone.tryon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class TryonResponse {

    @JsonProperty("tryon_id")
    private String tryonId;

    private String status;
    private int progress;

    @JsonProperty("user_image_id")
    private String userImageId;

    @JsonProperty("garment_id")
    private String garmentId;

    @JsonProperty("result_id")
    private String resultId;

    @JsonProperty("result_image_url")        // ← 추가: 프론트 polling 응답에 이미지 URL 포함
    private String resultImageUrl;

    private String message;
    private TryonErrorInfo error;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;
}