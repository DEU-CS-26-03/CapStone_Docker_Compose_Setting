package com.capstone.garment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GarmentUpdateRequest {
    private String category;
    private String brandKey;
    private String status;   // ACTIVE | HIDDEN
}