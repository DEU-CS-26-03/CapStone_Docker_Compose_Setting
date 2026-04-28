package com.capstone.tryon.python;

import com.capstone.tryon.python.dto.PythonInferRequest;
import com.capstone.tryon.python.dto.PythonInferResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class CatVtonWebClient implements CatVtonClient {

    private final RestClient restClient;

    @Override
    public PythonInferResponse infer(PythonInferRequest request) {
        return restClient.post()
                .uri("/infer")
                .body(request)
                .retrieve()
                .body(PythonInferResponse.class);
    }
}