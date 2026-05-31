package com.capstone.tryon.service;

import com.capstone.result.entity.Result;
import com.capstone.result.repository.ResultRepository;
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
    // [핵심]: DB 저장을 위해 ResultRepository 주입
    private final ResultRepository resultRepository;

    @Value("${FILE_RESULT_ROOT:/data/results}")
    private String resultRoot;

    public TryonAsyncProcessor(
            TryonService tryonService,
            @Qualifier("catVtonApiClient") CatVtonClient catVtonClient,
            ResultRepository resultRepository // 생성자 주입
    ) {
        this.tryonService = tryonService;
        this.catVtonClient = catVtonClient;
        this.resultRepository = resultRepository;
    }

    @Async("tryonTaskExecutor")
    public void process(String tryonId, String personPath, String clothPath, String clothType, Long userId) {
        log.info("[Async] >>> [1단계] 작업 시작 - ID: {}", tryonId);

        try {
            tryonService.updateStatusInNewTx(tryonId, "PROCESSING", 10, null, null, null);

            File pFile = new File(personPath);
            File cFile = new File(clothPath);
            if (!pFile.exists() || !cFile.exists()) {
                throw new RuntimeException("물리적 파일이 서버에 존재하지 않습니다.");
            }

            log.info("[Async] >>> [2단계] AI 서버로 파일 전송 중...");

            long startTime = System.currentTimeMillis();
            byte[] imageBytes = catVtonClient.infer(personPath, clothPath, clothType);
            int generationMs = (int) (System.currentTimeMillis() - startTime);

            if (imageBytes == null || imageBytes.length == 0) {
                throw new RuntimeException("AI 서버로부터 빈 이미지를 수신했습니다.");
            }

            log.info("[Async] >>> [3단계] AI 합성 완료, 파일 저장 시작");

            String filename = "result_" + tryonId + ".jpg";
            File dir = new File(resultRoot);
            if (!dir.exists()) dir.mkdirs();

            Path resultFilePath = Paths.get(resultRoot, filename);
            Files.write(resultFilePath, imageBytes);

            // 4. 저장 완료된 URL 생성
            String resultImageUrl = "https://apivirtualtryon.p-e.kr/uploads/results/" + filename;
            String resultId = "res_" + tryonId.substring(0,8);

            // [추가된 핵심 기능]: AI 성공 시 무조건 results 테이블에 데이터 Insert!
            Result newResult = Result.builder()
                    .resultId(resultId)
                    .tryonId(tryonId)
                    .userId(userId) // 어떤 유저의 결과인지 저장
                    .resultImageUrl(resultImageUrl)
                    .garmentCategory(clothType)
                    .generationMs(generationMs)
                    .rating(0) // 초기 별점은 0점
                    .deleted(false)
                    .build();
            resultRepository.save(newResult);

            // 5. TryonJob 테이블도 COMPLETED로 업데이트
            tryonService.updateStatusWithResultInNewTx(tryonId, resultId, resultImageUrl);

            log.info("[Async] <<< [최종성공] 피팅 결과 생성 완료: {}", resultImageUrl);

        } catch (Exception e) {
            log.error("[Async] !!! [실패] tryonId={} : {}", tryonId, e.getMessage());
            tryonService.updateStatusInNewTx(tryonId, "FAILED", 0, null, "PYTHON_ERROR", e.getMessage());
        }
    }
}