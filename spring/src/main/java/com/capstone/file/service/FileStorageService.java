//2) 저장 서비스
package com.capstone.file.service; // 변경

import com.capstone.config.FileStorageProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path personDir;
    private final Path garmentDir;
    private final Path resultDir;

    public FileStorageService(FileStorageProperties properties) {
        this.personDir = Paths.get(properties.getUploadRoot(), "person").toAbsolutePath().normalize();
        this.garmentDir = Paths.get(properties.getUploadRoot(), "garment").toAbsolutePath().normalize();
        this.resultDir = Paths.get(properties.getResultRoot()).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(personDir);
        Files.createDirectories(garmentDir);
        Files.createDirectories(resultDir);
    }

    public String savePersonImage(MultipartFile file) throws IOException {
        return save(file, personDir);
    }

    public String saveGarmentImage(MultipartFile file) throws IOException {
        return save(file, garmentDir);
    }

    public String saveResultImage(MultipartFile file) throws IOException {
        return save(file, resultDir);
    }

    private String save(MultipartFile file, Path dir) throws IOException {
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        String ext = "";

        int idx = original.lastIndexOf('.');
        if (idx >= 0) {
            ext = original.substring(idx);
        }

        String savedName = UUID.randomUUID() + ext;
        Path target = dir.resolve(savedName);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        return target.toString();
    }
}