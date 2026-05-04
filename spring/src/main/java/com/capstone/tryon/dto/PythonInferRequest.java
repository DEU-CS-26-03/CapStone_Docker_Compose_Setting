package com.capstone.tryon.python.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PythonInferRequest {

    @JsonProperty("person_image_url")
    private String personImageUrl;

    @JsonProperty("garment_image_url")
    private String garmentImageUrl;

    @JsonProperty("cloth_type")
    private String clothType;

    @JsonProperty("num_inference_steps")
    private Integer numInferenceSteps;

    @JsonProperty("guidance_scale")
    private Double guidanceScale;

    private Integer seed;
}