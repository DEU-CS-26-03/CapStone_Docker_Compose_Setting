package com.capstone.tryon.service;

import com.capstone.tryon.python.CatVtonClient;
import com.capstone.tryon.python.dto.PythonInferResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TryonAsyncProcessor {

    private final TryonService tryonService;
    private final CatVtonClient catVtonClient;

    @Async("tryonTaskExecutor")
    public void process(String tryonId, String personPath, String clothPath, String clothType) {
        log.info("[Async] tryonId={} 시작", tryonId);

        try {
            tryonService.updateStatusInNewTx(tryonId, "PROCESSING", 10, null, null, null);

            PythonInferResponse response = catVtonClient.infer(personPath, clothPath, clothType);

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

        } catch (Exception e) {
            log.error("[Async] tryonId={} 실패", tryonId, e);
            tryonService.updateStatusInNewTx(tryonId, "FAILED", 0, null, "PYTHON_ERROR", e.getMessage());
        }
    }
}