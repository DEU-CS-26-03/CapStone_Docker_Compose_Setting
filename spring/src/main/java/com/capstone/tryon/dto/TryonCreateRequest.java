package com.capstone.tryon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class TryonCreateRequest {

    @JsonProperty("user_image_id")
    private String userImageId;

    @JsonProperty("garment_id")
    private String garmentId;

    @JsonProperty("external_item_id")
    private String externalItemId;

    private MultipartFile personImage;
    private MultipartFile clothImage;
    private String clothType = "upper"; // 기본값
}