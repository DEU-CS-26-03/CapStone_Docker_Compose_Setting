// src/main/java/com/capstone/python/CatVtonWebClient.java
package com.capstone.python;

import com.capstone.python.dto.PythonInferRequest;
import com.capstone.python.dto.PythonInferResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatVtonWebClient implements CatVtonClient {

    private final WebClient pythonWebClient;

    @Override
    public PythonInferResponse infer(PythonInferRequest request) {
        try {
            return pythonWebClient.post()
                    .uri("/infer")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(PythonInferResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("[CatVton] Python 서버 오류 status={} body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Python 추론 서버 오류: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("[CatVton] Python 호출 실패: {}", e.getMessage());
            throw new RuntimeException("Python 추론 서버 연결 실패", e);
        }
    }
}