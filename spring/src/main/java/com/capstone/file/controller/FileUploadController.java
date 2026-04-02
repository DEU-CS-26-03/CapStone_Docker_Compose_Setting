//3) 업로드 API 예시
package com.capstone.file.controller; // 변경

import com.capstone.file.service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    public FileUploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/person")
    public ResponseEntity<?> uploadPerson(@RequestParam("file") MultipartFile file) throws IOException {
        String path = fileStorageService.savePersonImage(file);
        return ResponseEntity.ok(Map.of("savedPath", path));
    }

    @PostMapping("/garment")
    public ResponseEntity<?> uploadGarment(@RequestParam("file") MultipartFile file) throws IOException {
        String path = fileStorageService.saveGarmentImage(file);
        return ResponseEntity.ok(Map.of("savedPath", path));
    }
}