// src/main/java/com/capstone/python/config/PythonProperties.java
package com.capstone.python.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "python.inference")   // ← 수정: python → python.inference
public class PythonProperties {
    private String baseUrl;
    private int connectTimeoutMs = 3000;
    private int readTimeoutMs = 180000;
}