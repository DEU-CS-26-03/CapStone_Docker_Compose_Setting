package com.capstone.job.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class JobRedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KEY_PREFIX = "job:";
    private static final Duration TTL = Duration.ofHours(1);

    // [유지] Job 최초 생성
    public void createJob(String jobId, Long userId) {
        String key = KEY_PREFIX + jobId;
        Map<String, Object> jobData = Map.of(
                "status",    "QUEUED",
                "userId",    userId.toString(),
                "createdAt", LocalDateTime.now().toString()
        );
        redisTemplate.opsForHash().putAll(key, jobData);
        redisTemplate.expire(key, TTL);
    }

    // [수정] updateStatus: updatedAt 타임스탬프 함께 갱신
    // 기존: status 필드 하나만 put
    // 변경: status + updatedAt 두 필드를 putAll로 한 번에 저장 (Redis 왕복 1회로 감소)
    public void updateStatus(String jobId, String status) {
        String key = KEY_PREFIX + jobId;
        Map<String, Object> fields = Map.of(
                "status",    status,
                "updatedAt", LocalDateTime.now().toString()
        );
        redisTemplate.opsForHash().putAll(key, fields);
    }

    // [수정] setCompleted: put() 두 번 → putAll() 한 번으로 변경
    // 기존: opsForHash().put() 두 번 호출 (Redis 왕복 2회)
    // 변경: putAll()로 status + resultUrl + updatedAt 한 번에 저장 (원자성 보장)
    public void setCompleted(String jobId, String resultUrl) {
        String key = KEY_PREFIX + jobId;
        Map<String, Object> fields = Map.of(
                "status",    "COMPLETED",
                "resultUrl", resultUrl,
                "updatedAt", LocalDateTime.now().toString()
        );
        redisTemplate.opsForHash().putAll(key, fields);
    }

    // [유지] Job 전체 Hash 조회
    public Map<Object, Object> getJob(String jobId) {
        return redisTemplate.opsForHash().entries(KEY_PREFIX + jobId);
    }

    // [유지] 상태만 단건 조회
    public String getStatus(String jobId) {
        Object status = redisTemplate.opsForHash().get(KEY_PREFIX + jobId, "status");
        return status != null ? status.toString() : null;
    }

    // [유지] Job 수동 삭제
    public void deleteJob(String jobId) {
        redisTemplate.delete(KEY_PREFIX + jobId);
    }

    // [추가] Job 존재 여부 확인
    // TryonService에서 Redis 캐시 히트 판별 시 사용
    public boolean exists(String jobId) {
        Boolean hasKey = redisTemplate.hasKey(KEY_PREFIX + jobId);
        return Boolean.TRUE.equals(hasKey);
    }

    /**
     * [수정] TryonService용 통합 저장 메서드
     * 기존: opsForHash().put() 두 번 호출 (Redis 왕복 2회)
     * 변경: Map 빌더 패턴으로 putAll() 한 번에 처리 (원자성 보장)
     * - Map.of()는 null 값 허용 안 함 → HashMap 사용
     */
    public void save(String jobId, String status, int progress) {
        String key = KEY_PREFIX + jobId;

        Map<String, Object> fields = new HashMap<>();
        fields.put("status",    status);
        fields.put("progress",  String.valueOf(progress));
        fields.put("updatedAt", LocalDateTime.now().toString());

        redisTemplate.opsForHash().putAll(key, fields);

        // [유지] TTL: 키가 없거나(-2) TTL이 없는(-1) 경우에만 1시간 신규 설정
        Long expireSeconds = redisTemplate.getExpire(key);
        if (expireSeconds == null || expireSeconds < 0) {
            redisTemplate.expire(key, TTL);
        }
    }

    /**
     * [유지] TryonService 1차 캐시 조회용 (getStatus 위임)
     * null 반환 시 TryonService에서 DB fallback으로 전환
     */
    public String findStatusById(String jobId) {
        return getStatus(jobId);
    }

    /**
     * [유지] progress 조회
     * null 반환 시 TryonService에서 0으로 처리
     */
    public Integer findProgressById(String jobId) {
        Object progress = redisTemplate.opsForHash().get(KEY_PREFIX + jobId, "progress");
        if (progress == null) return null;
        try {
            return Integer.parseInt(progress.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * [유지] 최종 상태(COMPLETED/FAILED) 전환 시 짧은 TTL로 교체
     * TryonService에서 호출해 자동 만료 처리
     */
    public void expire(String jobId, long seconds) {
        redisTemplate.expire(KEY_PREFIX + jobId, Duration.ofSeconds(seconds));
    }
}