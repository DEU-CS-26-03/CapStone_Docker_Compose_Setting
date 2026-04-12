package com.capstone.tryon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TryonCreateRequest {

    @JsonProperty("user_image_id")
    @NotBlank(message = "user_image_id는 필수입니다.")
    private String userImageId;

    @JsonProperty("garment_id")
    @NotBlank(message = "garment_id는 필수입니다.")
    private String garmentId;
}