package com.capstone.tryon.python.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PythonInferResponse {
    private boolean success;

    @JsonProperty("result_image_url")
    private String resultImageUrl;

    @JsonProperty("result_object_key")
    private String resultObjectKey;

    private String message;
}