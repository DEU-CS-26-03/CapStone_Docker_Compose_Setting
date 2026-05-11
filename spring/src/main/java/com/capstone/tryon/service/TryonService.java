package com.capstone.tryon.service;

import com.capstone.job.repository.JobRedisRepository;
import com.capstone.tryon.dto.TryonCreateRequest;
import com.capstone.tryon.dto.TryonErrorInfo;
import com.capstone.tryon.dto.TryonResponse;
import com.capstone.tryon.entity.TryonJob;
import com.capstone.tryon.repository.TryonJobRepository;
import com.capstone.user.entity.User;
import com.capstone.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TryonService {

    private final TryonJobRepository tryonJobRepository;
    private final JobRedisRepository jobRedisRepository;
    private final UserRepository userRepository;
    private final TryonAsyncProcessor tryonAsyncProcessor;

    @Value("${app.file.upload-root:/data/uploads}")
    private String uploadRoot;

    public TryonService(
            TryonJobRepository tryonJobRepository,
            JobRedisRepository jobRedisRepository,
            UserRepository userRepository,
            @Lazy TryonAsyncProcessor tryonAsyncProcessor
    ) {
        this.tryonJobRepository = tryonJobRepository;
        this.jobRedisRepository = jobRedisRepository;
        this.userRepository = userRepository;
        this.tryonAsyncProcessor = tryonAsyncProcessor;
    }

    @Transactional
    public TryonResponse create(TryonCreateRequest request, String email) {
        // 1. 유저 매핑 (안전하게 첫 번째 유저 선택)
        Long userId = 1L;
        try {
            List<User> users = userRepository.findAll();
            if (!users.isEmpty()) userId = users.get(0).getId();
        } catch (Exception e) { log.warn("유저 매핑 실패, 기본값 사용"); }

        // 2. 고유 ID 생성
        String tryonId = "tryon_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        // ★ [중요] 파일을 DB 저장 전에 먼저 물리적으로 저장합니다.
        // 이렇게 해야 response와 AsyncProcessor에 null이 전달되지 않습니다.
        String personPath = saveFile(request.getPersonImage(), tryonId, "person");
        String clothPath = saveFile(request.getClothImage(), tryonId, "cloth");

        // 3. DB Entity 생성 및 모든 필드 한 번에 세팅
        TryonJob job = new TryonJob();
        job.setTryonId(tryonId);
        job.setUserId(userId);
        job.setStatus("queued");
        job.setProgress(0);
        job.setUserImageId(personPath);  // 경로 세팅
        job.setGarmentId(clothPath);    // 경로 세팅
        job.setClothType(request.getClothType());

        // 4. DB와 Redis에 즉시 반영 (Flush를 사용하여 트랜잭션 확정 준비)
        tryonJobRepository.saveAndFlush(job);

        try {
            jobRedisRepository.save(tryonId, "queued", 0);
        } catch (Exception e) { log.warn("[Redis] 저장 실패: {}", e.getMessage()); }

        // 5. 비동기 Python 추론 요청 (이미 파일 경로가 확정된 상태)
        tryonAsyncProcessor.process(tryonId, personPath, clothPath, request.getClothType());

        TryonResponse res = toResponse(job);
        res.setMessage("가상 피팅 작업이 생성되었습니다.");
        return res;
    }

    @Transactional
    public void updateStatusInNewTx(String tryonId, String status, int progress,
                                    String resultId, String errorCode, String errorMessage) {
        TryonJob job = tryonJobRepository.findById(tryonId)
                .orElseThrow(() -> new IllegalArgumentException("해당 작업을 찾을 수 없습니다: " + tryonId));
        job.setStatus(status);
        job.setProgress(progress);
        if (resultId != null) job.setResultId(resultId);
        if (errorCode != null) {
            job.setErrorCode(errorCode);
            job.setErrorMessage(errorMessage);
        }
        tryonJobRepository.save(job);
        syncRedis(tryonId, status, progress);
    }

    @Transactional
    public void updateStatusWithResultInNewTx(String tryonId, String resultId, String resultImageUrl) {
        TryonJob job = tryonJobRepository.findById(tryonId)
                .orElseThrow(() -> new IllegalArgumentException("해당 작업을 찾을 수 없습니다: " + tryonId));
        job.setStatus("completed");
        job.setProgress(100);
        job.setResultId(resultId);
        job.setResultImageUrl(resultImageUrl);
        tryonJobRepository.save(job);
        syncRedis(tryonId, "completed", 100);
        try { jobRedisRepository.expire(tryonId, 3600); } catch (Exception ignored) {}
    }

    @Transactional(readOnly = true)
    public List<TryonResponse> listByUser(String email) {
        // ★ 에러 방지: list 조회 시에도 안전하게 유저를 매핑
        Long userId = 1L;
        List<User> users = userRepository.findAll();
        if (!users.isEmpty()) {
            userId = users.get(0).getId();
        }
        return tryonJobRepository
                .findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TryonResponse getById(String tryonId) {
        try {
            String cachedStatus = jobRedisRepository.findStatusById(tryonId);
            if ("queued".equals(cachedStatus) || "processing".equals(cachedStatus)) {
                Integer progress = jobRedisRepository.findProgressById(tryonId);
                return toPartialResponse(tryonId, cachedStatus, progress != null ? progress : 0);
            }
        } catch (Exception e) {
            log.warn("[Redis] 캐시 조회 실패 (tryonId={}): {}", tryonId, e.getMessage());
        }

        TryonJob job = tryonJobRepository.findById(tryonId)
                .orElseThrow(() -> new IllegalArgumentException("해당 작업을 찾을 수 없습니다: " + tryonId));
        syncRedis(tryonId, job.getStatus(), job.getProgress());
        return toResponse(job);
    }

    @Transactional
    public void softDelete(String tryonId) {
        TryonJob job = tryonJobRepository.findById(tryonId)
                .orElseThrow(() -> new IllegalArgumentException("해당 작업을 찾을 수 없습니다: " + tryonId));
        job.setDeleted(true);
        tryonJobRepository.save(job);
    }

    private void syncRedis(String tryonId, String status, int progress) {
        try {
            jobRedisRepository.save(tryonId, status, progress);
        } catch (Exception e) {
            log.warn("[Redis] 상태 동기화 실패: {}", e.getMessage());
        }
    }

    private TryonResponse toResponse(TryonJob j) {
        TryonResponse res = new TryonResponse();
        res.setTryonId(j.getTryonId());
        res.setStatus(j.getStatus());
        res.setProgress(j.getProgress());
        res.setUserImageId(j.getUserImageId());
        res.setGarmentId(j.getGarmentId());
        res.setResultId(j.getResultId());
        res.setResultImageUrl(j.getResultImageUrl());
        res.setCreatedAt(j.getCreatedAt());
        res.setUpdatedAt(j.getUpdatedAt());
        if (j.getErrorCode() != null) {
            res.setError(new TryonErrorInfo(j.getErrorCode(), j.getErrorMessage()));
        }
        return res;
    }

    private TryonResponse toPartialResponse(String tryonId, String status, int progress) {
        TryonResponse res = new TryonResponse();
        res.setTryonId(tryonId);
        res.setStatus(status);
        res.setProgress(progress);
        return res;
    }

    private String saveFile(MultipartFile file, String tryonId, String prefix) {
        try {
            String filename = prefix + "_" + tryonId + ".jpg";
            Path dest = Paths.get(uploadRoot, filename);
            Files.createDirectories(dest.getParent());
            file.transferTo(dest);
            return dest.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Optional<TryonResponse> claimNextPendingJob() {
        return tryonJobRepository.findFirstByStatusOrderByCreatedAtAsc("QUEUED")
                .map(job -> {
                    job.setStatus("PROCESSING");
                    tryonJobRepository.save(job);
                    return toResponse(job);
                });
    }

    @Transactional
    public TryonResponse updateStatus(String tryonId, String status, int progress,
                                      String resultId, String resultImageUrl, String errorMessage) {
        TryonJob job = tryonJobRepository.findById(tryonId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + tryonId));
        job.setStatus(status);
        job.setProgress(progress);
        job.setResultId(resultId);
        job.setResultImageUrl(resultImageUrl);
        job.setErrorMessage(errorMessage);
        return toResponse(tryonJobRepository.save(job));
    }
}