package com.capstone.tryon.service;

import com.capstone.tryon.python.CatVtonClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
public class TryonAsyncProcessor {

    private final TryonService tryonService;
    private final CatVtonClient catVtonClient;

    // application.yml 에 설정된 결과 저장 경로 및 퍼블릭 URL
    @Value("${FILE_RESULT_ROOT:/data/results}")
    private String resultRoot;

    @Value("${APP_UPLOAD_PUBLIC_BASE_URL:https://apivirtualtryon.p-e.kr}")
    private String publicBaseUrl;

    public TryonAsyncProcessor(
            TryonService tryonService,
            @Qualifier("catVtonApiClient") CatVtonClient catVtonClient
    ) {
        this.tryonService = tryonService;
        this.catVtonClient = catVtonClient;
    }

    @Async("tryonTaskExecutor")
    public void process(String tryonId, String personPath, String clothPath, String clothType) {
        log.info("[Async] tryonId={} 시작", tryonId);

        try {
            Thread.sleep(1000);
            tryonService.updateStatusInNewTx(tryonId, "PROCESSING", 10, null, null, null);

            // ★ 수정됨: 파이썬에서 JSON이 아닌 진짜 이미지 파일(바이트 배열)을 리턴받음
            byte[] imageBytes = catVtonClient.infer(personPath, clothPath, clothType);

            // 1. 우분투 서버의 /data/results 폴더에 파일 저장
            String resultId = "result_" + tryonId.substring(0, 8);
            String filename = resultId + ".jpg";

            File dir = new File(resultRoot);
            if (!dir.exists()) dir.mkdirs();

            Path resultFilePath = Paths.get(resultRoot, filename);
            Files.write(resultFilePath, imageBytes);

            // 2. 프론트엔드가 접근할 수 있는 최종 URL 생성 (예: https://apivirtualtryon.p-e.kr/results/xxx.jpg)
            // (경로는 Nginx 설정에 따라 /results/ 또는 /files/results/ 등으로 맞춰주세요)
            String resultImageUrl = "https://apivirtualtryon.p-e.kr/uploads/results/" + filename;

            tryonService.updateStatusWithResultInNewTx(tryonId, resultId, resultImageUrl);

            log.info("[Async] tryonId={} 완료, 최종 이미지 URL={}", tryonId, resultImageUrl);

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("[Async] tryonId={} 실패", tryonId, e);
            tryonService.updateStatusInNewTx(tryonId, "FAILED", 0, null, "PYTHON_ERROR", e.getMessage());
        }
    }
}