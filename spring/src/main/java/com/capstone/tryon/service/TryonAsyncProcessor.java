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

    @Value("${FILE_RESULT_ROOT:/data/results}")
    private String resultRoot;

    public TryonAsyncProcessor(
            TryonService tryonService,
            @Qualifier("catVtonApiClient") CatVtonClient catVtonClient
    ) {
        this.tryonService = tryonService;
        this.catVtonClient = catVtonClient;
    }

    @Async("tryonTaskExecutor")
    public void process(String tryonId, String personPath, String clothPath, String clothType) {
        log.info("[Async] >>> [1단계] 작업 시작 - ID: {}", tryonId);

        try {
            // 상태 업데이트: PROCESSING 10%
            tryonService.updateStatusInNewTx(tryonId, "PROCESSING", 10, null, null, null);

            // ★ [체크] 파일이 실제 우분투 경로에 존재하는지 확인 (없으면 여기서 터짐)
            File pFile = new File(personPath);
            File cFile = new File(clothPath);
            if (!pFile.exists() || !cFile.exists()) {
                throw new RuntimeException("물리적 파일이 서버에 존재하지 않습니다.");
            }

            log.info("[Async] >>> [2단계] AI 서버로 파일 전송 중... (Size: {} bytes)", pFile.length());

            // 2. Python 서버 호출 (RestTemplate이 파일을 바이트로 읽어서 보냅니다)
            byte[] imageBytes = catVtonClient.infer(personPath, clothPath, clothType);

            if (imageBytes == null || imageBytes.length == 0) {
                throw new RuntimeException("AI 서버로부터 빈 이미지를 수신했습니다.");
            }
            log.info("[Async] >>> [3단계] AI 합성 완료, 파일 저장 시작");

            // 3. 결과 저장
            String filename = "result_" + tryonId + ".jpg";
            File dir = new File(resultRoot);
            if (!dir.exists()) dir.mkdirs();

            Path resultFilePath = Paths.get(resultRoot, filename);
            Files.write(resultFilePath, imageBytes);

            // 4. 최종 결과 업데이트
            String resultImageUrl = "https://apivirtualtryon.p-e.kr/uploads/results/" + filename;
            tryonService.updateStatusWithResultInNewTx(tryonId, "res_" + tryonId.substring(0,8), resultImageUrl);

            log.info("[Async] <<< [최종성공] 피팅 결과 생성 완료: {}", resultImageUrl);

        } catch (Exception e) {
            log.error("[Async] !!! [실패] tryonId={} : {}", tryonId, e.getMessage());
            // 에러 발생 시 반드시 FAILED로 바꿔야 프론트엔드의 무한 로딩이 멈춥니다.
            tryonService.updateStatusInNewTx(tryonId, "FAILED", 0, null, "PYTHON_ERROR", e.getMessage());
        }
    }
}