package com.capstone.tryon.controller;

import com.capstone.tryon.dto.TryonCreateRequest;
import com.capstone.tryon.dto.TryonResponse;
import com.capstone.tryon.service.TryonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tryons")
@RequiredArgsConstructor
public class TryonController {

    private final TryonService tryonService;

    // ★ NPE 방지용 안전한 이메일 추출 도우미 메서드
    private String getSafeEmail(Authentication authentication) {
        if (authentication != null && authentication.getName() != null && !authentication.getName().equals("anonymousUser")) {
            return authentication.getName();
        }
        return "guest@capstone.com"; // 비회원용 더미 이메일
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TryonResponse> create(
            @RequestPart("personImage") MultipartFile personImage,
            @RequestPart("clothImage") MultipartFile clothImage,
            @RequestParam(value = "clothType", required = false) String clothType,
            Authentication authentication) {

        String email = getSafeEmail(authentication);

        TryonCreateRequest request = new TryonCreateRequest();
        request.setPersonImage(personImage);
        request.setClothImage(clothImage);
        request.setClothType(clothType != null ? clothType : "upper");

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(tryonService.create(request, email));
    }

    @GetMapping
    public ResponseEntity<List<TryonResponse>> list(Authentication authentication) {
        String email = getSafeEmail(authentication);
        return ResponseEntity.ok(tryonService.listByUser(email));
    }

    @GetMapping("/{tryonId}")
    public ResponseEntity<TryonResponse> getById(@PathVariable String tryonId) {
        return ResponseEntity.ok(tryonService.getById(tryonId));
    }

    @DeleteMapping("/{tryonId}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String tryonId) {
        tryonService.softDelete(tryonId);
        return ResponseEntity.ok(Map.of("message", "작업이 삭제되었습니다."));
    }
}