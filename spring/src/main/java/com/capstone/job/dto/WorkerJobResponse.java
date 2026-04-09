//Python 워커가 다음 작업 받을 때 응답
package com.capstone.job.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WorkerJobResponse {

    @JsonProperty("tryon_id")
    private String tryonId;

    @JsonProperty("user_image_id")
    private String userImageId;

    @JsonProperty("garment_id")
    private String garmentId;

    // 실제 파일 경로 (Python이 파일 직접 읽을 때 사용)
    @JsonProperty("user_image_path")
    private String userImagePath;

    @JsonProperty("garment_path")
    private String garmentPath;

    public WorkerJobResponse() {}

    public WorkerJobResponse(String tryonId, String userImageId, String garmentId,
                             String userImagePath, String garmentPath) {
        this.tryonId = tryonId;
        this.userImageId = userImageId;
        this.garmentId = garmentId;
        this.userImagePath = userImagePath;
        this.garmentPath = garmentPath;
    }

    public String getTryonId() { return tryonId; }
    public void setTryonId(String tryonId) { this.tryonId = tryonId; }
    public String getUserImageId() { return userImageId; }
    public void setUserImageId(String userImageId) { this.userImageId = userImageId; }
    public String getGarmentId() { return garmentId; }
    public void setGarmentId(String garmentId) { this.garmentId = garmentId; }
    public String getUserImagePath() { return userImagePath; }
    public void setUserImagePath(String userImagePath) { this.userImagePath = userImagePath; }
    public String getGarmentPath() { return garmentPath; }
    public void setGarmentPath(String garmentPath) { this.garmentPath = garmentPath; }
}