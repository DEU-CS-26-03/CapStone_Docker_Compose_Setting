package com.capstone.tryon.dto;

import com.capstone.tryon.domain.TryonStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
public class GetTryonResponse {

    @JsonProperty("tryon_id")
    private String tryonId;

    private TryonStatus status;

    private int progress;

    @JsonProperty("result_id")
    private String resultId;

    @JsonProperty("result_image_url")
    private String resultImageUrl;

    private String message;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;
}