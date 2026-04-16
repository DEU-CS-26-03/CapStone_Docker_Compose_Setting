package com.capstone.userimage.controller;

import com.capstone.userimage.dto.UserImageResponse;
import com.capstone.userimage.service.UserImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user-images")
public class UserImageController {

    private final UserImageService service;

    public UserImageController(UserImageService service) {
        this.service = service;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<UserImageResponse> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "view", defaultValue = "front") String view
    ) throws IOException {
        return ResponseEntity.ok(service.upload(file, view));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserImageResponse> getById(@PathVariable("id") String imageId) {
        return ResponseEntity.ok(service.getById(imageId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable("id") String imageId) {
        service.softDelete(imageId);
        return ResponseEntity.ok(Map.of("message", "이미지가 삭제되었습니다."));
    }
}