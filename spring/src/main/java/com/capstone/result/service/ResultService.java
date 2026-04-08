package com.capstone.result.service;

import com.capstone.result.dto.ResultResponse;
import com.capstone.result.entity.TryonResult;
import com.capstone.result.repository.TryonResultRepository;
import org.springframework.stereotype.Service;

@Service
public class ResultService {

    private final TryonResultRepository repository;

    public ResultService(TryonResultRepository repository) {
        this.repository = repository;
    }

    public ResultResponse getById(String resultId) {
        TryonResult entity = repository.findById(resultId)
                .orElseThrow(() -> new IllegalArgumentException("결과를 찾을 수 없습니다: " + resultId));
        return new ResultResponse(
                entity.getResultId(), entity.getTryonId(), entity.getStatus(),
                entity.getResultUrl(), entity.getThumbnailUrl(), entity.getCreatedAt()
        );
    }
}