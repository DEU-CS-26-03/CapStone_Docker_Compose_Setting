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
// 외부 설정을 객체에 바인딩
@EnableConfigurationProperties(FileStorageProperties.class)
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}