package com.capstone.storage.service;

import com.capstone.storage.dto.LocalUploadResponse;
import com.capstone.storage.dto.PresignRequest;
import com.capstone.storage.dto.PresignResponse;
import com.capstone.storage.entity.UploadSession;
import com.capstone.storage.repository.UploadSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final UploadSessionRepository uploadSessionRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.public-base-url}")
    private String publicBaseUrl;

    public PresignResponse createUploadSession(Long userId, PresignRequest request) {
        String token = UUID.randomUUID().toString().replace("-", "");
        String objectKey = buildObjectKey(userId, request.getUploadType(), request.getFileName());

        UploadSession session = new UploadSession();
        session.setToken(token);
        session.setUserId(userId);
        session.setObjectKey(objectKey);
        session.setFileName(request.getFileName());
        session.setContentType(request.getContentType());
        session.setUploadType(request.getUploadType());
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        session.setUploaded(false);

        uploadSessionRepository.save(session);

        return PresignResponse.builder()
                .uploadUrl("/api/v1/uploads/" + token)
                .uploadToken(token)
                .objectKey(objectKey)
                .expiresIn(600)
                .method("PUT")
                .build();
    }

    public LocalUploadResponse uploadByToken(String token, InputStream inputStream, String contentType) throws Exception {
        UploadSession session = uploadSessionRepository.findById(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 업로드 토큰입니다."));

        if (session.isUploaded()) {
            throw new IllegalStateException("이미 사용된 업로드 토큰입니다.");
        }

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("업로드 토큰이 만료되었습니다.");
        }

        Path savePath = Path.of(uploadDir, session.getObjectKey());
        Files.createDirectories(savePath.getParent());

        byte[] bytes = StreamUtils.copyToByteArray(inputStream);
        Files.write(savePath, bytes);

        session.setUploaded(true);
        uploadSessionRepository.save(session);

        return LocalUploadResponse.builder()
                .objectKey(session.getObjectKey())
                .fileUrl(publicBaseUrl + "/" + session.getObjectKey())
                .contentType(contentType != null ? contentType : session.getContentType())
                .size(bytes.length)
                .build();
    }

    private String buildObjectKey(Long userId, String uploadType, String fileName) {
        String today = LocalDate.now().toString().replace("-", "");
        String ext = extractExtension(fileName);
        String safeType = switch ((uploadType == null ? "" : uploadType).toUpperCase()) {
            case "USER_IMAGE" -> "user-image";
            case "GARMENT" -> "garment";
            default -> "misc";
        };
        return safeType + "/" + userId + "/" + today + "/" + UUID.randomUUID() + ext;
    }

    private String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "";
        return fileName.substring(fileName.lastIndexOf("."));
    }
}