package com.capstone.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InternalJobResponse {
    private String tryonId;
    private String userImagePath;
    private String garmentPath;
    private String status;
}