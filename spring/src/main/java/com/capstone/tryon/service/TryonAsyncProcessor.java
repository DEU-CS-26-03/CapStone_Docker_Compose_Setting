package com.capstone.tryon.service;

import com.capstone.tryon.python.CatVtonClient;
import com.capstone.tryon.python.dto.PythonInferResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;

@Slf4j
@Component
public class TryonAsyncProcessor {

    private final TryonService tryonService;
    private final CatVtonClient catVtonClient;

    public TryonAsyncProcessor(
            TryonService tryonService,
            @Qualifier("catVtonApiClient") CatVtonClient catVtonClient
    ) {
        this.tryonService = tryonService;
        this.catVtonClient = catVtonClient;
    }

    @Async("tryonTaskExecutor")
    public void process(String tryonId, String personPath, String clothPath, String clothType) {
        log.info("[Async] tryonId={} 시작 - DB 커밋 대기 중...", tryonId);

        try {
            // 메인 스레드가 DB 저장을 완료할 수 있도록 1초(1000ms) 대기
            Thread.sleep(1000);

            tryonService.updateStatusInNewTx(tryonId, "PROCESSING", 10, null, null, null);

            // ★ 폭탄 제거: 로컬 경로(/data/uploads/...)를 공개 URL(https://...)로 변환
            // (Spring WebMvc 설정에 따라 /files/ 또는 /uploads/ 로 매핑되어야 합니다)
            String baseUrl = "https://apivirtualtryon.p-e.kr/files/"; // 또는 /uploads/

            String personFileName = new File(personPath).getName();
            String clothFileName = new File(clothPath).getName();

            // 파이썬은 이 URL에 접속해서 이미지를 다운로드하여 추론합니다.
            String personUrl = baseUrl + personFileName;
            String clothUrl = clothPath.contains("http") ? clothPath : baseUrl + clothFileName;

            log.info("[Async] tryonId={} 파이썬으로 추론 요청 전송 시작", tryonId);
            log.info(" - 사람 이미지 URL: {}", personUrl);
            log.info(" - 옷 이미지 URL: {}", clothUrl);

            // 변환된 URL을 파이썬으로 전송!
            PythonInferResponse response = catVtonClient.infer(personUrl, clothUrl, clothType);

            if (response == null) {
                throw new RuntimeException("Python 응답이 null 입니다.");
            }

            if (!response.isSuccess()) {
                throw new RuntimeException("Python 추론 실패: " + response.getError());
            }

            String resultImageUrl = response.getResultImageUrl();
            if (resultImageUrl == null || resultImageUrl.isBlank()) {
                throw new RuntimeException("Python result_image_url 이 비어 있습니다.");
            }

            String resultId = "result_" + tryonId.substring(0, 8);
            tryonService.updateStatusWithResultInNewTx(tryonId, resultId, resultImageUrl);

            log.info("[Async] tryonId={} 완료 url={}", tryonId, resultImageUrl);

        } catch (InterruptedException ie) {
            log.error("[Async] tryonId={} 스레드 대기 중 인터럽트 발생", tryonId, ie);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("[Async] tryonId={} 실패", tryonId, e);
            tryonService.updateStatusInNewTx(tryonId, "FAILED", 0, null, "PYTHON_ERROR", e.getMessage());
        }
    }
}