// com/capstone/tryon/service/TryonAsyncProcessor.java
package com.capstone.tryon.service;

import com.capstone.tryon.python.CatVtonClient;
import com.capstone.tryon.python.dto.PythonInferRequest;
import com.capstone.tryon.python.dto.PythonInferResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * 별도 Bean으로 분리해야 @Async 프록시가 적용됨.
     * TryonService 내부에서 직접 호출하면 프록시 우회로 동기 실행됨.
     */
    @Async("tryonTaskExecutor")
    public void process(String tryonId, String personPath, String clothPath, String clothType) {
        log.info("[Async] tryonId={} 시작", tryonId);
        try {
            tryonService.updateStatusInNewTx(tryonId, "processing", 10, null, null, null);

            // Python 호출 → byte[] 수신
            byte[] resultBytes = catVtonClient.inferWithFiles(personPath, clothPath, clothType);

            // Ubuntu에 결과 이미지 저장
            String resultFileName = tryonId + "_result.jpg";
            Path resultFilePath = Paths.get(resultRoot, resultFileName);
            Files.createDirectories(resultFilePath.getParent());
            Files.write(resultFilePath, resultBytes);

            // DB에 저장할 URL (Spring 정적 서빙 경로)
            String resultImageUrl = baseUrl + "/uploads/results/" + resultFileName;
            String resultId = "result_" + tryonId.substring(0, 8);

            tryonService.updateStatusWithResultInNewTx(tryonId, resultId, resultImageUrl);
            log.info("[Async] tryonId={} 완료, url={}", tryonId, resultImageUrl);

        } catch (Exception e) {
            log.error("[Async] tryonId={} 실패", tryonId, e);
            tryonService.updateStatusInNewTx(tryonId, "failed", 0, null, "PYTHON_ERROR", e.getMessage());
        }
    }
}