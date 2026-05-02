package com.capstone.tryon.python;

import com.capstone.tryon.python.dto.PythonInferRequest;
import com.capstone.tryon.python.dto.PythonInferResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatVtonWebClient implements CatVtonClient {

    private final RestTemplate restTemplate;

    @Value("${AI_SERVER_URL:http://ai-server:8000}")
    private String aiServerUrl;

    @Value("${USE_MOCK:false}")
    private boolean useMock;

    @Override
    public PythonInferResponse infer(PythonInferRequest request) {
        if (useMock) {
            log.info("[CatVtonWebClient] MOCK 모드 — 실제 추론 생략");
            return mockResponse();
        }

        String url = aiServerUrl + "/infer";
        log.info("[CatVtonWebClient] POST {} person={} garment={}",
                url, request.getPersonImageUrl(), request.getGarmentImageUrl());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("ngrok-skip-browser-warning", "true");

        ResponseEntity<PythonInferResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                PythonInferResponse.class
        );

        if (response.getBody() == null) {
            throw new RuntimeException("Python 서버 응답이 비어있습니다.");
        }
        return response.getBody();
    }

    private PythonInferResponse mockResponse() {
        PythonInferResponse res = new PythonInferResponse();
        res.setResultImageUrl("https://picsum.photos/seed/tryon/768/1024");
        res.setSuccess(true);
        return res;
    }
    @Override
    public byte[] inferWithFiles(String personPath, String clothPath, String clothType) {
        if (useMock) {
            log.info("[CatVtonWebClient] MOCK 모드 — 더미 바이트 반환");
            return new byte[]{};
        }

        String url = aiServerUrl + "/infer";
        log.info("[CatVtonWebClient] inferWithFiles POST {} person={} cloth={}", url, personPath, clothPath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("ngrok-skip-browser-warning", "true");
        headers.setAccept(List.of(MediaType.APPLICATION_OCTET_STREAM, MediaType.IMAGE_JPEG));

        Map<String, String> body = Map.of(
                "person_image_path", personPath,
                "cloth_image_path",  clothPath,
                "cloth_type",        clothType != null ? clothType : "upper"
        );

        ResponseEntity<byte[]> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                byte[].class
        );

        if (response.getBody() == null || response.getBody().length == 0) {
            throw new RuntimeException("Python 서버에서 빈 이미지가 반환되었습니다.");
        }
        return response.getBody();
    }
}