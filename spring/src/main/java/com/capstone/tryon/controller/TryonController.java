// com/capstone/tryon/controller/TryonController.java
package com.capstone.tryon.controller;

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

    /**
     * POST /api/v1/tryons
     * Content-Type: multipart/form-data
     * - personImage : 사람 이미지 파일
     * - clothImage  : 의상 이미지 파일
     * - clothType   : upper / lower / overall (선택, 기본 upper)
     */

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TryonResponse> create(
            @RequestPart("personImage") MultipartFile personImage,
            @RequestPart("clothImage") MultipartFile clothImage,
            @RequestPart(value = "clothType", required = false) String clothType,
            Authentication authentication) {

        String email = authentication.getName();
        TryonCreateRequest request = new TryonCreateRequest();
        request.setPersonImage(personImage);
        request.setClothImage(clothImage);
        request.setClothType(clothType != null ? clothType : "upper");

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(tryonService.create(request, email));
    }

    // GET /api/v1/tryons — 내 작업 목록
    @GetMapping
    public ResponseEntity<List<TryonResponse>> list(Authentication authentication) {
        return ResponseEntity.ok(tryonService.listByUser(authentication.getName()));
    }

    // GET /api/v1/tryons/{tryonId} — 작업 상태 폴링
    @GetMapping("/{tryonId}")
    public ResponseEntity<TryonResponse> getById(@PathVariable String tryonId) {
        return ResponseEntity.ok(tryonService.getById(tryonId));
    }

    // DELETE /api/v1/tryons/{tryonId}
    @DeleteMapping("/{tryonId}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String tryonId) {
        tryonService.softDelete(tryonId);
        return ResponseEntity.ok(Map.of("message", "작업이 삭제되었습니다."));
    }
}