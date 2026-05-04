package com.capstone.tryon.python;

import com.capstone.tryon.python.dto.PythonInferRequest;
import com.capstone.tryon.python.dto.PythonInferResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
public class CatVtonWebClient implements CatVtonClient {

    private final RestTemplate restTemplate;

    @Value("${AISERVERURL:http://ai-server:8000}")
    private String aiServerUrl;

    @Override
    public PythonInferResponse infer(String personPath, String clothPath, String clothType) {
        String url = aiServerUrl + "/infer";

        PythonInferRequest request = PythonInferRequest.builder()
                .personImageUrl(personPath)
                .garmentImageUrl(clothPath)
                .clothType(clothType != null ? clothType : "upper")
                .numInferenceSteps(30)
                .guidanceScale(2.5)
                .seed(42)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<PythonInferRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<PythonInferResponse> response =
                restTemplate.postForEntity(url, entity, PythonInferResponse.class);

        if (response.getBody() == null) {
            throw new RuntimeException("Python 응답 body가 null 입니다.");
        }

        return response.getBody();
    }
}