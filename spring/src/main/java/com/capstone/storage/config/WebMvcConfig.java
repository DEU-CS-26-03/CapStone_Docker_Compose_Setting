package com.capstone.storage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.garments-dir:/data/uploads/garments}")
    private String garmentsDir;

    @Value("${file.result-root:/data/results}")
    private String resultRoot;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String garmentsLocation = garmentsDir.endsWith("/") ? garmentsDir : garmentsDir + "/";
        String resultsLocation = resultRoot.endsWith("/") ? resultRoot : resultRoot + "/";

        registry.addResourceHandler("/files/garments/**")
                .addResourceLocations("file:" + garmentsLocation);

        registry.addResourceHandler("/uploads/results/**")
                .addResourceLocations("file:" + resultsLocation);

        registry.addResourceHandler("/uploads/user-images/**")
                .addResourceLocations("file:/data/uploads/"); // 실제 파일 저장 경로
    }
}