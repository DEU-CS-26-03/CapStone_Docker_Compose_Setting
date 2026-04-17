package com.capstone.storage.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PresignRequest {
    private String fileName;
    private String contentType;
    private String uploadType;
}