package com.capstone.storage.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocalUploadResponse {
    private String objectKey;
    private String fileUrl;
    private String contentType;
    private long size;
}