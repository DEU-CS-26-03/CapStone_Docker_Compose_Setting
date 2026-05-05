package com.capstone.favorite.controller;

import com.capstone.favorite.dto.FavoriteResponse;
import com.capstone.favorite.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService service;

    // GET /api/v1/favorites — 내 즐겨찾기 목록 조회
    @GetMapping
    public ResponseEntity<List<FavoriteResponse>> list(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
        return ResponseEntity.ok(service.list(authentication.getName()));
    }

    // POST /api/v1/favorites — 즐겨찾기 추가
    @PostMapping
    public ResponseEntity<Map<String, String>> add(
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        if (authentication == null) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
        service.add(authentication.getName(), body.get("garmentId"));
        return ResponseEntity.ok(Map.of("message", "즐겨찾기에 추가되었습니다."));
    }


    // DELETE /api/v1/favorites/{garmentId} — 즐겨찾기 제거
    @DeleteMapping("/{garmentId}")
    public ResponseEntity<Map<String, String>> remove(
            @PathVariable String garmentId,
            Authentication authentication
    ) {
        if (authentication == null) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
        service.remove(authentication.getName(), garmentId);
        return ResponseEntity.ok(Map.of("message", "즐겨찾기에서 제거되었습니다."));
    }
}