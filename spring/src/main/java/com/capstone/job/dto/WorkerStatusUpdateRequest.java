//Python 워커가 상태 보낼 때 요청 바디
package com.capstone.job.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WorkerStatusUpdateRequest {

    // "processing" | "completed" | "failed"
    private String status;

    // 0 ~ 100
    private int progress;

    // completed 시 결과 ID
    @JsonProperty("result_id")
    private String resultId;

    // failed 시 에러 코드
    @JsonProperty("error_code")
    private String errorCode;

    // failed 시 에러 메시지
    @JsonProperty("error_message")
    private String errorMessage;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public String getResultId() { return resultId; }
    public void setResultId(String resultId) { this.resultId = resultId; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}