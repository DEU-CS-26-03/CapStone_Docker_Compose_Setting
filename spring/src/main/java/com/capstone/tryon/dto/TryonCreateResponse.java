package com.capstone.tryon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TryonCreateResponse {

    @JsonProperty("tryon_id")
    private String tryonId;

    private String status;
    private int progress;
    private String message;
}