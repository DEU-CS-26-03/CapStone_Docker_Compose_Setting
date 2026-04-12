//1)설정 클래스
package com.capstone.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {
    private String uploadRoot;
    private String resultRoot;

    public String getUploadRoot() {
        return uploadRoot;
    }

    public void setUploadRoot(String uploadRoot) {
        this.uploadRoot = uploadRoot;
    }

    public String getResultRoot() {
        return resultRoot;
    }

    public void setResultRoot(String resultRoot) {
        this.resultRoot = resultRoot;
    }
}