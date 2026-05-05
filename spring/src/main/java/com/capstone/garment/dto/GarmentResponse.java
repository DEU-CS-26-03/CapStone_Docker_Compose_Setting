package com.capstone.garment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
public class GarmentResponse {

    @JsonProperty("garment_id")
    private String garmentId;

    private String status;

    @JsonProperty("source_type")
    private String sourceType;

    private String category;
    private String name;
    private Integer price;

    @JsonProperty("content_type")
    private String contentType;

    @JsonProperty("file_url")
    private String fileUrl;

    @JsonProperty("brand_key")
    private String brandKey;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}