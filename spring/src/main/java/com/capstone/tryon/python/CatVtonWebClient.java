package com.capstone.tryon.python;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    // ─────────────────────────────────────────────
    // STEP 1: submit → job_id 반환
    // ─────────────────────────────────────────────
    @Override
    public String submitJob(String personPath, String clothPath, String clothType) {
        if (useMock) {
            log.info("[CatVtonWebClient] MOCK — submitJob");
            return "mock-job-id";
        }

        String url = aiServerUrl + "/tryon/submit";
        log.info("[CatVtonWebClient] submitJob POST {}", url);

        try {
            byte[] personBytes = Files.readAllBytes(Paths.get(personPath));
            byte[] clothBytes  = Files.readAllBytes(Paths.get(clothPath));

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("person_image", new ByteArrayResource(personBytes) {
                @Override public String getFilename() { return "person.jpg"; }
            });
            body.add("cloth_image", new ByteArrayResource(clothBytes) {
                @Override public String getFilename() { return "cloth.jpg"; }
            });
            body.add("cloth_type", clothType != null ? clothType : "upper");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("ngrok-skip-browser-warning", "true");

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            if (response.getBody() == null || !response.getBody().containsKey("job_id")) {
                throw new RuntimeException("Python submit 응답에 job_id가 없습니다.");
            }
            return (String) response.getBody().get("job_id");

        } catch (IOException e) {
            throw new RuntimeException("이미지 파일 읽기 실패: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────
    // STEP 2: job 상태 polling → "DONE" 확인
    // ─────────────────────────────────────────────
    @Override
    public String pollJobStatus(String jobId) {
        if (useMock) return "DONE";

        String url = aiServerUrl + "/tryon/jobs/" + jobId;

        ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET,
                new HttpEntity<>(makeBaseHeaders()),
                Map.class
        );

        if (response.getBody() == null) {
            throw new RuntimeException("Python jobs 응답이 비어있습니다.");
        }
        return (String) response.getBody().get("status");
    }

    // ─────────────────────────────────────────────
    // STEP 3: 완료 후 결과 이미지 byte[] 수신
    // ─────────────────────────────────────────────
    @Override
    public byte[] fetchResultImage(String jobId) {
        if (useMock) return new byte[]{};

        String url = aiServerUrl + "/tryon/result/" + jobId + "/image";
        log.info("[CatVtonWebClient] fetchResultImage GET {}", url);

        ResponseEntity<byte[]> response = restTemplate.exchange(
                url, HttpMethod.GET,
                new HttpEntity<>(makeBaseHeaders()),
                byte[].class
        );

        if (response.getBody() == null || response.getBody().length == 0) {
            throw new RuntimeException("결과 이미지가 비어있습니다. jobId=" + jobId);
        }
        return response.getBody();
    }

    // ─────────────────────────────────────────────
    // 공통 헤더
    // ─────────────────────────────────────────────
    private HttpHeaders makeBaseHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("ngrok-skip-browser-warning", "true");
        return headers;
    }
}