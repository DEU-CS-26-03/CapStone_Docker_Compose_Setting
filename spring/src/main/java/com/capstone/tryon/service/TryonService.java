package com.capstone.tryon.service;

import com.capstone.job.repository.JobRedisRepository;
import com.capstone.tryon.dto.*;
import com.capstone.tryon.entity.TryonJob;
import com.capstone.tryon.repository.TryonJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Service
public class TryonService {

    private static final Logger log = LoggerFactory.getLogger(TryonService.class);

    private final TryonJobRepository tryonJobRepository;
    private final JobRedisRepository jobRedisRepository;

    public TryonService(TryonJobRepository tryonJobRepository,
                        JobRedisRepository jobRedisRepository) {
        this.tryonJobRepository = tryonJobRepository;
        this.jobRedisRepository = jobRedisRepository;
    }

    @Transactional
    public TryonResponse create(TryonCreateRequest request) {
        validateCreateRequest(request);

        Optional<TryonJob> activeJob = tryonJobRepository.findFirstByStatusIn(
                Arrays.asList("queued", "processing")
        );
        if (activeJob.isPresent()) {
            throw new IllegalStateException("현재 다른 작업이 처리 중입니다.");
        }

        String tryonId = "tryon_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        TryonJob job = new TryonJob();
        job.setTryonId(tryonId);
        job.setStatus("queued");
        job.setProgress(0);
        job.setUserImageId(request.getUserImageId());
        job.setGarmentId(request.getGarmentId());

        tryonJobRepository.save(job);

        try {
            jobRedisRepository.save(tryonId, "queued", 0);
        } catch (Exception e) {
            log.warn("[Redis] 캐시 저장 실패 (tryonId={}): {}", tryonId, e.getMessage());
        }

        TryonResponse res = toResponse(job);
        res.setMessage("Try-on job created successfully.");
        return res;
    }

    private void validateCreateRequest(TryonCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("요청 본문이 비어 있습니다.");
        }
        if (isBlank(request.getUserImageId())) {
            throw new IllegalArgumentException("user_image_id는 필수입니다.");
        }
        if (isBlank(request.getGarmentId())) {
            throw new IllegalArgumentException("garment_id는 필수입니다.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public TryonResponse getById(String tryonId) {
        try {
            String cachedStatus = jobRedisRepository.findStatusById(tryonId);
            if (cachedStatus != null) {
                if ("queued".equals(cachedStatus) || "processing".equals(cachedStatus)) {
                    Integer cachedProgress = jobRedisRepository.findProgressById(tryonId);
                    return toPartialResponse(tryonId, cachedStatus,
                            cachedProgress != null ? cachedProgress : 0);
                }
            }
        } catch (Exception e) {
            log.warn("[Redis] 캐시 조회 실패 (tryonId={}): {}", tryonId, e.getMessage());
        }

        TryonJob job = tryonJobRepository.findById(tryonId)
                .orElseThrow(() -> new IllegalArgumentException("해당 작업을 찾을 수 없습니다: " + tryonId));

        try {
            jobRedisRepository.save(tryonId, job.getStatus(), job.getProgress());
        } catch (Exception e) {
            log.warn("[Redis] 캐시 갱신 실패 (tryonId={}): {}", tryonId, e.getMessage());
        }

        return toResponse(job);
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
            log.warn("[Redis] 상태 동기화 실패 (tryonId={}): {}", tryonId, e.getMessage());
        }

        return toResponse(job);
    }

    @Transactional
    public Optional<TryonResponse> claimNextPendingJob() {
        Optional<TryonJob> job = tryonJobRepository.findFirstByStatusIn(
                Arrays.asList("queued")
        );

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

    @Transactional
    public TryonResponse createJob(TryonCreateRequest request) {
        return create(request);
    }

    public TryonResponse getJob(String tryonId) {
        return getById(tryonId);
    }

    private TryonResponse toResponse(TryonJob j) {
        TryonResponse res = new TryonResponse();
        res.setTryonId(j.getTryonId());
        res.setStatus(j.getStatus());
        res.setProgress(j.getProgress());
        res.setUserImageId(j.getUserImageId());
        res.setGarmentId(j.getGarmentId());
        res.setResultId(j.getResultId());
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