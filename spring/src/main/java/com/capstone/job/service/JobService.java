//Redis 캐시 유틸 + TryonService 연결 브릿지
package com.capstone.job.service;

import com.capstone.job.dto.WorkerJobResponse;
import com.capstone.job.repository.JobRedisRepository;
import com.capstone.tryon.entity.TryonJob;
import com.capstone.tryon.repository.TryonJobRepository;
import com.capstone.userimage.repository.UserImageRepository;
import com.capstone.garment.repository.GarmentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JobService {

    private static final Logger log = LoggerFactory.getLogger(JobService.class);

    private final JobRedisRepository jobRedisRepository;
    private final TryonJobRepository tryonJobRepository;
    private final UserImageRepository userImageRepository;
    private final GarmentRepository garmentRepository;

    @Value("${file.upload.user-images-dir}")
    private String userImagesDir;

    @Value("${file.upload.garments-dir}")
    private String garmentsDir;

    // ─────────────────────────────────────────────
    // Python 워커: 다음 pending 작업 claim
    // ─────────────────────────────────────────────

    /**
     * queued 상태 작업을 하나 가져와 processing으로 변경
     * Python 워커가 GET /api/internal/jobs/next 호출 시 사용
     */
    @Transactional
    public Optional<WorkerJobResponse> claimNextJob() {
        Optional<TryonJob> jobOpt = tryonJobRepository
                .findFirstByStatusIn(List.of("queued"));

        if (jobOpt.isEmpty()) {
            return Optional.empty();
        }

        TryonJob job = jobOpt.get();
        job.setStatus("processing");
        job.setProgress(0);
        tryonJobRepository.save(job);

        // Redis 캐시 동기화
        try {
            jobRedisRepository.save(job.getTryonId(), "processing", 0);
        } catch (Exception e) {
            log.warn("[Redis] claim 상태 캐시 실패: {}", e.getMessage());
        }

        // 파일 실제 경로 조합해서 워커에게 전달
        String userImageFilename = extractFilename(
                userImageRepository.findById(job.getUserImageId())
                        .map(img -> img.getFileUrl()).orElse("")
        );
        String garmentFilename = extractFilename(
                garmentRepository.findById(job.getGarmentId())
                        .map(g -> g.getFileUrl()).orElse("")
        );

        WorkerJobResponse response = new WorkerJobResponse(
                job.getTryonId(),
                job.getUserImageId(),
                job.getGarmentId(),
                userImagesDir + "/" + userImageFilename,
                garmentsDir + "/" + garmentFilename
        );

        return Optional.of(response);
    }

    // ─────────────────────────────────────────────
    // Redis 캐시 유틸 (TryonService에서 직접 쓸 수도 있음)
    // ─────────────────────────────────────────────

    public void cacheStatus(String jobId, String status, int progress) {
        try {
            jobRedisRepository.save(jobId, status, progress);
        } catch (Exception e) {
            log.warn("[Redis] 캐시 저장 실패 (jobId={}): {}", jobId, e.getMessage());
        }
    }

    public String getCachedStatus(String jobId) {
        try {
            return jobRedisRepository.findStatusById(jobId);
        } catch (Exception e) {
            log.warn("[Redis] 캐시 조회 실패 (jobId={}): {}", jobId, e.getMessage());
            return null;
        }
    }

    public Integer getCachedProgress(String jobId) {
        try {
            return jobRedisRepository.findProgressById(jobId);
        } catch (Exception e) {
            log.warn("[Redis] progress 조회 실패 (jobId={}): {}", jobId, e.getMessage());
            return null;
        }
    }

    public void expireCache(String jobId, long seconds) {
        try {
            jobRedisRepository.expire(jobId, seconds);
        } catch (Exception e) {
            log.warn("[Redis] TTL 설정 실패 (jobId={}): {}", jobId, e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // 유틸
    // ─────────────────────────────────────────────
    private String extractFilename(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return "";
        return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
    }
}