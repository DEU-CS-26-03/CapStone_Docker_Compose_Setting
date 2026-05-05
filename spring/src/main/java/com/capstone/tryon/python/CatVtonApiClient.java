package com.capstone.tryon.python;

import com.capstone.tryon.python.dto.PythonInferResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component("catVtonApiClient")
@RequiredArgsConstructor
public class CatVtonApiClient implements CatVtonClient {

    private final RestTemplate restTemplate;

    // compose.yml에 설정된 PYTHON_INFERENCE_BASE_URL을 가져옵니다. (없으면 도커 내부망 사용)
    @Value("${python.inference.base-url:http://python-app:8000}")
    private String pythonBaseUrl;

    @Override
    public PythonInferResponse infer(String personPath, String clothPath, String clothType) {

        // 1. FastAPI가 기대하는 파라미터 조합 (ex: /infer?user_image_path=...&garment_image_path=...)
        String url = UriComponentsBuilder.fromHttpUrl(pythonBaseUrl + "/infer")
                .queryParam("user_image_path", personPath)
                .queryParam("garment_image_path", clothPath)
                .toUriString();

        log.info("[CatVtonClient] 파이썬 서버로 추론 요청 시작: {}", url);

        try {
            HttpHeaders headers = new HttpHeaders();
            // ★ 핵심 해결책: 무료 ngrok의 HTML 경고 페이지를 무시하고 API로 바로 통과시키는 마법의 헤더
            headers.set("ngrok-skip-browser-warning", "69420");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // POST 요청 전송
            ResponseEntity<PythonInferResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    PythonInferResponse.class
            );

            // 2. 응답 검증
            if (response.getBody() == null) {
                throw new RuntimeException("Python 응답 body가 null 입니다.");
            }

            log.info("[CatVtonClient] 파이썬 추론 요청 성공! 상태 코드: {}", response.getStatusCode());
            return response.getBody();

        } catch (Exception e) {
            log.error("[CatVtonClient] 파이썬 서버 통신 중 치명적 오류 발생: {}", e.getMessage());
            // TryonAsyncProcessor가 이 에러 메시지를 잡아서 DB에 FAILED 상태로 저장합니다.
            throw new RuntimeException("Python API 호출 실패: " + e.getMessage(), e);
        }
    }
}