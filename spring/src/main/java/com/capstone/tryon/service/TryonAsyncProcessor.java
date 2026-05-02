// com/capstone/tryon/service/TryonAsyncProcessor.java
package com.capstone.tryon.service;

import com.capstone.tryon.python.CatVtonClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
@RequiredArgsConstructor
public class TryonAsyncProcessor {

    @Value("${file.result-root:/data/results}")
    private String resultRoot;

    @Value("${file.result-base-url:http://localhost:8080}")
    private String baseUrl;

    private final TryonService tryonService;
    private final CatVtonClient catVtonClient;

    @Async("tryonTaskExecutor")
    public void process(String tryonId, String personPath, String clothPath, String clothType) {
        log.info("[Async] tryonId={} 시작", tryonId);
        try {
            tryonService.updateStatusInNewTx(tryonId, "PROCESSING", 10, null, null, null);

            // 1. Python 호출 → byte[] 수신
            byte[] resultBytes = catVtonClient.inferWithFiles(personPath, clothPath, clothType);

            // 2. Ubuntu 파일시스템에 저장
            String resultFileName = tryonId + "_result.jpg";
            Path resultFilePath = Paths.get(resultRoot, resultFileName);
            Files.createDirectories(resultFilePath.getParent());
            Files.write(resultFilePath, resultBytes);

            // 3. DB에 URL 기록 + COMPLETED
            String resultImageUrl = baseUrl + "/uploads/results/" + resultFileName;
            String resultId = "result_" + tryonId.substring(0, 8);

            tryonService.updateStatusWithResultInNewTx(tryonId, resultId, resultImageUrl);
            log.info("[Async] tryonId={} 완료, url={}", tryonId, resultImageUrl);

        } catch (Exception e) {
            log.error("[Async] tryonId={} 실패", tryonId, e);
            tryonService.updateStatusInNewTx(tryonId, "FAILED", 0, null, "PYTHON_ERROR", e.getMessage());
        }
    }
}