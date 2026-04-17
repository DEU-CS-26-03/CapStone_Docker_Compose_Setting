// src/main/java/com/capstone/tryon/service/TryonService.java
package com.capstone.tryon.service;

import com.capstone.job.repository.JobRedisRepository;
import com.capstone.python.CatVtonClient;
import com.capstone.python.dto.PythonInferRequest;
import com.capstone.python.dto.PythonInferResponse;
import com.capstone.tryon.dto.TryonCreateRequest;
import com.capstone.tryon.dto.TryonErrorInfo;
import com.capstone.tryon.dto.TryonResponse;
import com.capstone.tryon.entity.TryonJob;
import com.capstone.tryon.repository.TryonJobRepository;
import com.capstone.user.entity.User;
import com.capstone.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TryonService {

    private final TryonJobRepository tryonJobRepository;
    private final JobRedisRepository jobRedisRepository;
    private final UserRepository userRepository;
    private final CatVtonClient catVtonClient;           // ← 추가

    // ─────────────────────────────────────────────────
    // POST /api/v1/tryons  — 작업 생성 + Python 비동기 호출
    // ─────────────────────────────────────────────────
    @Transactional
    public TryonResponse create(TryonCreateRequest request, String email) {
        if (request.getGarmentId() == null && request.getExternalItemKey() == null) {
            throw new IllegalArgumentException("garmentId 또는 externalItemKey 중 하나는 필수입니다.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String tryonId = "tryon_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        TryonJob job = new TryonJob();
        job.setTryonId(tryonId);
        job.setUserId(user.getId());
        job.setStatus("queued");
        job.setProgress(0);
        job.setUserImageId(request.getUserImageId());
        job.setGarmentId(request.getGarmentId());
        job.setExternalItemKey(request.getExternalItemKey());
        tryonJobRepository.save(job);

        try {
            jobRedisRepository.save(tryonId, "queued", 0);
        } catch (Exception e) {
            log.warn("[Redis] 캐시 저장 실패 (tryonId={}): {}", tryonId, e.getMessage());
        }

        // ← 추가: 트랜잭션 커밋 후 비동기로 Python 호출
        processTryonAsync(tryonId, request.getUserImageId(), request.getGarmentId());

        TryonResponse res = toResponse(job);
        res.setMessage("가상 피팅 작업이 생성되었습니다.");
        return res;
    }

    // ─────────────────────────────────────────────────
    // Python /infer 비동기 호출 — queued → processing → completed/failed
    // ─────────────────────────────────────────────────
    @Async                                               // ← 추가: AsyncConfig 필요 (아래 참고)
    public void processTryonAsync(String tryonId, String userImageId, String garmentId) {
        try {
            // 1. processing 전환
            updateStatusInNewTx(tryonId, "processing", 10, null, null, null);

            // 2. userImageId, garmentId → Python에 넘길 URL 조회
            //    현재 UserImage, Garment 엔티티가 없으므로 userImageId를 직접 URL로 사용
            //    → 2순위 이미지 업로드 API 완성 후 fileUrl 조회로 교체 예정
            String userImageUrl  = resolveImageUrl(userImageId);
            String garmentUrl    = resolveGarmentUrl(garmentId);

            // 3. Python POST /infer 호출
            PythonInferResponse response = catVtonClient.infer(
                    PythonInferRequest.builder()
                            .userImagePath(userImageUrl)
                            .garmentImagePath(garmentUrl)
                            .build()
            );

            // 4. 결과 저장 → completed
            String resultId = "result_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            updateStatusWithResultInNewTx(tryonId, resultId, response.getResultImageUrl());

        } catch (Exception e) {
            log.error("[TryonAsync] Python 호출 실패 tryonId={}: {}", tryonId, e.getMessage());
            updateStatusInNewTx(tryonId, "failed", 0, null, "PYTHON_ERROR", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────
    // 상태 갱신 — 별도 트랜잭션 (async 메서드와 트랜잭션 분리)
    // ─────────────────────────────────────────────────
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

        try {
            jobRedisRepository.save(tryonId, status, progress);
            if ("completed".equals(status) || "failed".equals(status)) {
                jobRedisRepository.expire(tryonId, 60);
            }
        } catch (Exception e) {
            log.warn("[Redis] 상태 동기화 실패: {}", e.getMessage());
        }
    }

    @Transactional
    public void updateStatusWithResultInNewTx(String tryonId, String resultId, String resultImageUrl) {
        TryonJob job = tryonJobRepository.findById(tryonId)
                .orElseThrow(() -> new IllegalArgumentException("해당 작업을 찾을 수 없습니다: " + tryonId));
        job.setStatus("completed");
        job.setProgress(100);
        job.setResultId(resultId);
        job.setResultImageUrl(resultImageUrl);           // ← 추가 필드
        tryonJobRepository.save(job);

        try {
            jobRedisRepository.save(tryonId, "completed", 100);
            jobRedisRepository.expire(tryonId, 60);
        } catch (Exception e) {
            log.warn("[Redis] completed 캐시 실패: {}", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────
    // URL 해석 헬퍼 — 2순위 완성 후 DB 조회로 교체
    // ─────────────────────────────────────────────────
    private String resolveImageUrl(String userImageId) {
        // TODO: 2순위 UserImage 엔티티 완성 후 → userImageRepository.findById(userImageId).getFileUrl()
        return userImageId;   // 현재는 클라이언트가 직접 URL을 userImageId에 담아 보내는 임시 방식
    }

    private String resolveGarmentUrl(String garmentId) {
        // TODO: 2순위 Garment 엔티티 완성 후 → garmentRepository.findById(garmentId).getFileUrl()
        return garmentId;     // 동일
    }

    // ─────────────────────────────────────────────────
    // 나머지 기존 메서드 — 패키지 import만 변경, 로직 동일
    // ─────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<TryonResponse> listByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return tryonJobRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TryonResponse getById(String tryonId) {
        try {
            String cachedStatus = jobRedisRepository.findStatusById(tryonId);
            if (cachedStatus != null &&
                    ("queued".equals(cachedStatus) || "processing".equals(cachedStatus))) {
                Integer progress = jobRedisRepository.findProgressById(tryonId);
                return toPartialResponse(tryonId, cachedStatus, progress != null ? progress : 0);
            }
        } catch (Exception e) {
            log.warn("[Redis] 캐시 조회 실패 (tryonId={}): {}", tryonId, e.getMessage());
        }

        TryonJob job = tryonJobRepository.findById(tryonId)
                .orElseThrow(() -> new IllegalArgumentException("해당 작업을 찾을 수 없습니다: " + tryonId));
        try {
            jobRedisRepository.save(tryonId, job.getStatus(), job.getProgress());
        } catch (Exception e) {
            log.warn("[Redis] 캐시 갱신 실패: {}", e.getMessage());
        }
        return toResponse(job);
    }

    @Transactional
    public void softDelete(String tryonId) {
        TryonJob job = tryonJobRepository.findById(tryonId)
                .orElseThrow(() -> new IllegalArgumentException("해당 작업을 찾을 수 없습니다: " + tryonId));
        job.setDeleted(true);
        tryonJobRepository.save(job);
    }

    @Transactional
    public TryonResponse updateStatus(String tryonId, String status, int progress,
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
        try {
            jobRedisRepository.save(tryonId, status, progress);
            if ("completed".equals(status) || "failed".equals(status)) {
                jobRedisRepository.expire(tryonId, 60);
            }
        } catch (Exception e) {
            log.warn("[Redis] 상태 동기화 실패: {}", e.getMessage());
        }
        return toResponse(job);
    }

    @Transactional
    public Optional<TryonResponse> claimNextPendingJob() {
        Optional<TryonJob> job = tryonJobRepository.findFirstByStatusIn(List.of("queued"));
        job.ifPresent(j -> {
            j.setStatus("processing");
            j.setProgress(0);
            tryonJobRepository.save(j);
            try {
                jobRedisRepository.save(j.getTryonId(), "processing", 0);
            } catch (Exception e) {
                log.warn("[Redis] processing 상태 캐시 실패: {}", e.getMessage());
            }
        });
        return job.map(this::toResponse);
    }

    private TryonResponse toResponse(TryonJob j) {
        TryonResponse res = new TryonResponse();
        res.setTryonId(j.getTryonId());
        res.setStatus(j.getStatus());
        res.setProgress(j.getProgress());
        res.setUserImageId(j.getUserImageId());
        res.setGarmentId(j.getGarmentId());
        res.setResultId(j.getResultId());
        res.setResultImageUrl(j.getResultImageUrl());    // ← 추가
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
}