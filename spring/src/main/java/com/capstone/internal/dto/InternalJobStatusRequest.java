package com.capstone.internal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InternalJobStatusRequest {
    private String status;       // queued | processing | completed | failed
    private int progress;        // 0~100
    private String resultId;     // completed 시 결과 ID
    private String errorCode;    // failed 시
    private String errorMessage; // failed 시
}