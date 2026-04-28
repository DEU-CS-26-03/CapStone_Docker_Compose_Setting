package com.capstone.tryon.python.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PythonInferRequest {

    @JsonProperty("person_image_url")
    private String personImageUrl;

    @JsonProperty("garment_image_url")
    private String garmentImageUrl;
}