package com.capstone.tryon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TryonCreateRequest {

    @JsonProperty("user_image_id")
    private String userImageId;

    @JsonProperty("garment_id")
    private String garmentId;

    public String getUserImageId() { return userImageId; }
    public void setUserImageId(String userImageId) { this.userImageId = userImageId; }
    public String getGarmentId() { return garmentId; }
    public void setGarmentId(String garmentId) { this.garmentId = garmentId; }
}