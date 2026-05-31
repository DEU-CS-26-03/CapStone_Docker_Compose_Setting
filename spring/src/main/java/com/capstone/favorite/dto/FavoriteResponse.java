package com.capstone.favorite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
public class FavoriteResponse {

    @JsonProperty("garment_id")
    private String garmentId;

    private String status;

    @JsonProperty("source_type")
    private String sourceType;

    private String category;
    private String filename;

    @JsonProperty("file_url")
    private String fileUrl;

    @JsonProperty("brand_key")
    private String brandKey;

    @JsonProperty("garment_name")
    private String garmentName;

    @JsonProperty("favorited_at")
    private OffsetDateTime favoritedAt;
}