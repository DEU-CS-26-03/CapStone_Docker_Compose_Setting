package com.capstone.tryon.python;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;

@Slf4j
@Component("catVtonApiClient")
@RequiredArgsConstructor
public class CatVtonApiClient implements CatVtonClient {

    private final RestTemplate restTemplate;

    @Value("${python.inference.base-url:http://python-app:8000}")
    private String pythonBaseUrl;

    @Override
    public byte[] infer(String personPath, String clothPath, String clothType) {
        String url = pythonBaseUrl + "/infer";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("ngrok-skip-browser-warning", "69420");

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("person_image", new FileSystemResource(new File(personPath)));
            body.add("cloth_image", new FileSystemResource(new File(clothPath)));

            // ★ 수정됨: 프론트엔드에서 받은 값을 그대로 전송하되, 안전하게 소문자 처리
            if (clothType == null || clothType.isEmpty()) {
                body.add("cloth_type", "upper");
            } else {
                body.add("cloth_type", clothType.toLowerCase());
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            log.info("[CatVtonClient] 파이썬으로 추론 요청 전송 | Mode: {} | URL: {}", clothType, url);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    byte[].class
            );

            if (response.getBody() == null || response.getBody().length == 0) {
                throw new RuntimeException("Python 서버에서 빈 이미지가 반환되었습니다.");
            }

            log.info("[CatVtonClient] 파이썬 추론 성공! 이미지 수신 완료 (사이즈: {} bytes)", response.getBody().length);
            return response.getBody();

        } catch (Exception e) {
            log.error("[CatVtonClient] 통신 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("Python API 호출 실패: " + e.getMessage(), e);
        }
    }
}