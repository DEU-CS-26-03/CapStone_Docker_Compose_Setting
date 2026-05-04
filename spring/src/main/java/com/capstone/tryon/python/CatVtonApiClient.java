package com.capstone.tryon.python;

import com.capstone.tryon.python.dto.PythonInferRequest;
import com.capstone.tryon.python.dto.PythonInferResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.nio.file.Path;

@Slf4j
@Component
public class CatVtonApiClient implements CatVtonClient {

    private final RestTemplate restTemplate;

    @Value("${python.api.base-url}")
    private String pythonBaseUrl;

    @Value("${app.base-url}")
    private String springBaseUrl;

    public CatVtonApiClient(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofMinutes(5))
                .build();
    }

    @Override
    public PythonInferResponse infer(String personPath, String clothPath, String clothType) {
        String personImageUrl = toPublicUploadUrl(personPath);
        String garmentImageUrl = toPublicUploadUrl(clothPath);

        PythonInferRequest request = PythonInferRequest.builder()
                .personImageUrl(personImageUrl)
                .garmentImageUrl(garmentImageUrl)
                .clothType(clothType)
                .numInferenceSteps(30)
                .guidanceScale(2.5)
                .seed(42)
                .build();

        String url = pythonBaseUrl + "/infer";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<PythonInferRequest> entity = new HttpEntity<>(request, headers);

        log.info("[CatVTON] POST {} personUrl={} garmentUrl={} clothType={}",
                url, personImageUrl, garmentImageUrl, clothType);

        ResponseEntity<PythonInferResponse> response =
                restTemplate.postForEntity(url, entity, PythonInferResponse.class);

        PythonInferResponse body = response.getBody();

        if (body == null) {
            throw new RuntimeException("Python 응답 body가 null 입니다.");
        }

        return body;
    }

    private String toPublicUploadUrl(String absolutePath) {
        String fileName = Path.of(absolutePath).getFileName().toString();
        return springBaseUrl + "/uploads/" + fileName;
    }
}