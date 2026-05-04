package com.capstone.tryon.python.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PythonInferResponse {

    @JsonProperty("result_image_url")
    private String resultImageUrl;

    private boolean success;
    private String error;
}