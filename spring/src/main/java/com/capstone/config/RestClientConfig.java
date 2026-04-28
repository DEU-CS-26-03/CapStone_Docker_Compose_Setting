package com.capstone.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient(
            @Value("${python.base-url:}") String pythonBaseUrl
    ) {
        RestClient.Builder builder = RestClient.builder();

        if (pythonBaseUrl != null && !pythonBaseUrl.isBlank()) {
            builder.baseUrl(pythonBaseUrl);
        }

        return builder.build();
    }
}