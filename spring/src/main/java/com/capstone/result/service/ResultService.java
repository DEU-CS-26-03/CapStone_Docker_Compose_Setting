package com.capstone.result.service;

import com.capstone.garment.dto.GarmentResponse;
import com.capstone.garment.entity.Garment;
import com.capstone.garment.repository.GarmentRepository;
import com.capstone.result.dto.FeedbackRequest;
import com.capstone.result.dto.FeedbackResponse;
import com.capstone.result.dto.ResultResponse;
import com.capstone.result.entity.Result;
import com.capstone.result.repository.ResultRepository;
import com.capstone.user.entity.User;
import com.capstone.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResultService {

    private final ResultRepository resultRepository;
    private final UserRepository userRepository;
    private final GarmentRepository garmentRepository;

    @Transactional(readOnly = true)
    public List<ResultResponse> listByUser(String email) {
        User user = findUser(email);
        return resultRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ResultResponse getById(String resultId) {
        Result result = findResult(resultId);
        return toResponse(result);
    }

    @Transactional
    public void softDelete(String resultId, String email) {
        User user = findUser(email);
        Result result = findResult(resultId);

        if (!result.getUserId().equals(user.getId())) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }
        result.setDeleted(true);
        resultRepository.save(result);
    }

    @Transactional
    public FeedbackResponse saveFeedback(String resultId, FeedbackRequest request, String email) {
        Result result = findResult(resultId);

        result.setRating(request.getRating());
        result.setComment(request.getComment());

        // 별점 기반 추천 모드 결정
        // 4~5점 = SIMILAR, 1~2점 = CONTRAST, 3점 = MIXED
        String mode;
        if (request.getRating() >= 4) {
            mode = "SIMILAR";
        } else if (request.getRating() <= 2) {
            mode = "CONTRAST";
        } else {
            mode = "MIXED";
        }
        result.setRecommendationMode(mode);
        resultRepository.save(result);

        return new FeedbackResponse(request.getRating(), mode, "피드백이 저장되었습니다.");
    }

    @Transactional(readOnly = true)
    public List<GarmentResponse> getRecommendations(String resultId) {
        Result result = findResult(resultId);

        if (result.getRecommendationMode() == null) {
            throw new IllegalStateException("아직 피드백이 등록되지 않았습니다.");
        }

        // 추천 모드에 따라 의류 필터링
        // SIMILAR: 같은 카테고리, CONTRAST: 다른 카테고리, MIXED: 전체
        List<Garment> garments;
        if ("SIMILAR".equals(result.getRecommendationMode())) {
            garments = garmentRepository.findTop10ByCategoryAndStatusOrderByCreatedAtDesc(
                    result.getGarmentCategory(), "ACTIVE");
        } else if ("CONTRAST".equals(result.getRecommendationMode())) {
            garments = garmentRepository.findTop10ByStatusAndCategoryNotOrderByCreatedAtDesc(
                    "ACTIVE", result.getGarmentCategory());
        } else {
            garments = garmentRepository.findTop10ByStatusOrderByCreatedAtDesc("ACTIVE");
        }

        return garments.stream().map(this::toGarmentResponse).collect(Collectors.toList());
    }

    private Result findResult(String resultId) {
        return resultRepository.findById(resultId)
                .orElseThrow(() -> new IllegalArgumentException("결과를 찾을 수 없습니다: " + resultId));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private ResultResponse toResponse(Result r) {
        return new ResultResponse(
                r.getResultId(),
                r.getTryonId(),
                r.getResultImageUrl(),
                r.getResultThumbnailUrl(),
                r.getGenerationMs(),
                r.getCreatedAt()
        );
    }

    private GarmentResponse toGarmentResponse(Garment g) {
        return new GarmentResponse(
                String.valueOf(g.getGarmentId()), g.getStatus(), g.getSourceType(),
                g.getCategory(), g.getFilename(), g.getContentType(),
                g.getFileUrl(), g.getBrandKey(), g.getCreatedAt()
        );
    }
}