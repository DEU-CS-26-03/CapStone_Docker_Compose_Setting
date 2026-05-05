package com.capstone.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(10)) // 연결 대기 시간 10초로 약간 여유있게
                // ★ 핵심 수정: AI 파이프라인(약 36초 소요)을 충분히 기다릴 수 있도록 3분(180초)으로 연장
                .setReadTimeout(Duration.ofSeconds(180))
                .additionalInterceptors(new NgrokHeaderInterceptor())
                .build();
    }

    // 모든 RestTemplate 요청에 ngrok 헤더 자동 추가
    static class NgrokHeaderInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public ClientHttpResponse intercept(
                HttpRequest request,
                byte[] body,
                ClientHttpRequestExecution execution
        ) throws IOException {
            request.getHeaders().set("ngrok-skip-browser-warning", "true");
            return execution.execute(request, body);
        }
    }
}