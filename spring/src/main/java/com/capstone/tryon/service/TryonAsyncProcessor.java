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

            // STEP 1: Python에 추론 작업 제출 → pythonJobId 수신
            String pythonJobId = catVtonClient.submitJob(personPath, clothPath, clothType);
            log.info("[Async] tryonId={} pythonJobId={} 제출 완료", tryonId, pythonJobId);

            // STEP 2: DONE 될 때까지 polling (5초 간격, 최대 10분)
            String pythonStatus = "";
            for (int i = 0; i < 120; i++) {
                Thread.sleep(5000);
                pythonStatus = catVtonClient.pollJobStatus(pythonJobId);
                log.info("[Async] tryonId={} pythonJobId={} status={}", tryonId, pythonJobId, pythonStatus);

                if ("DONE".equals(pythonStatus))   break;
                if ("FAILED".equals(pythonStatus)) throw new RuntimeException("Python 추론 실패: jobId=" + pythonJobId);
            }
            if (!"DONE".equals(pythonStatus)) {
                throw new RuntimeException("Python 추론 타임아웃: jobId=" + pythonJobId);
            }

            // STEP 3: 결과 이미지 byte[] 수신 → Ubuntu에 저장
            byte[] resultBytes = catVtonClient.fetchResultImage(pythonJobId);
            String resultFileName = tryonId + "_result.jpg";
            Path resultFilePath = Paths.get(resultRoot, resultFileName);
            Files.createDirectories(resultFilePath.getParent());
            Files.write(resultFilePath, resultBytes);

            // STEP 4: DB resultImageUrl 기록 + COMPLETED
            String resultImageUrl = baseUrl + "/uploads/results/" + resultFileName;
            String resultId = "result_" + tryonId.substring(0, 8);
            tryonService.updateStatusWithResultInNewTx(tryonId, resultId, resultImageUrl);
            log.info("[Async] tryonId={} 완료 url={}", tryonId, resultImageUrl);

        } catch (Exception e) {
            log.error("[Async] tryonId={} 실패", tryonId, e);
            tryonService.updateStatusInNewTx(tryonId, "FAILED", 0, null, "PYTHON_ERROR", e.getMessage());
        }
    }
}