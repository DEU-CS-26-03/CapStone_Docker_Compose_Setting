package com.capstone.tryon.service;

import com.capstone.tryon.dto.*;
import com.capstone.tryon.entity.TryonJob;
import com.capstone.tryon.repository.TryonJobRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Service
public class TryonService {

    private final TryonJobRepository repository;

    public TryonService(TryonJobRepository repository) {
        this.repository = repository;
    }

    /**
     * 가상 피팅 작업 생성
     * - 현재 queued 또는 processing 작업이 있으면 409 Conflict 예외
     * - 없으면 queued 상태로 신규 작업 생성
     */
    public TryonResponse create(TryonCreateRequest request) {
        // MVP: 동시에 1개만 허용
        Optional<TryonJob> activeJob = repository.findFirstByStatusIn(
                Arrays.asList("queued", "processing")
        );
        if (activeJob.isPresent()) {
            throw new IllegalStateException("ALREADY_ACTIVE");
        }

        String tryonId = "tryon_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        TryonJob job = new TryonJob();
        job.setTryonId(tryonId);
        job.setStatus("queued");
        job.setProgress(0);
        job.setUserImageId(request.getUserImageId());
        job.setGarmentId(request.getGarmentId());

        repository.save(job);

        TryonResponse res = toResponse(job);
        res.setMessage("Try-on job created successfully.");
        return res;
    }

    /**
     * 작업 상태 조회
     */
    public TryonResponse getById(String tryonId) {
        TryonJob job = repository.findById(tryonId)
                .orElseThrow(() -> new IllegalArgumentException("해당 작업을 찾을 수 없습니다: " + tryonId));
        return toResponse(job);
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
}