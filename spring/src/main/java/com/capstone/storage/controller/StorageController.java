package com.capstone.storage.controller;

import com.capstone.storage.dto.LocalUploadResponse;
import com.capstone.storage.dto.PresignRequest;
import com.capstone.storage.dto.PresignResponse;
import com.capstone.storage.service.StorageService;
import com.capstone.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.InputStream;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @PostMapping("/images/presign")
    public PresignResponse presign(
            @AuthenticationPrincipal User user,
            @RequestBody PresignRequest request
    ) {
        return storageService.createUploadSession(user.getId(), request);
    }

    @PutMapping(value = "/uploads/{token}", consumes = MediaType.ALL_VALUE)
    public LocalUploadResponse upload(
            @PathVariable String token,
            HttpServletRequest request
    ) throws Exception {
        try (InputStream inputStream = request.getInputStream()) {
            return storageService.uploadByToken(token, inputStream, request.getContentType());
        }
    }
}