package com.capstone.storage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.file.result-root:/data/results}")
    private String resultRoot;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /uploads/results/** → Ubuntu 실제 경로 매핑
        registry.addResourceHandler("/uploads/results/**")
                .addResourceLocations("file:" + resultRoot + "/");
    }
}