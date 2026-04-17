// src/main/java/com/capstone/python/dto/PythonInferResponse.java
package com.capstone.python.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PythonInferResponse {
    @JsonProperty("result_image_url")
    private String resultImageUrl;

    @JsonProperty("result_object_key")
    private String resultObjectKey;

    private String message;
}