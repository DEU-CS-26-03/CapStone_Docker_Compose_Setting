package com.capstone.userimage.service;

import com.capstone.userimage.dto.UserImageResponse;
import com.capstone.userimage.entity.UserImage;
import com.capstone.userimage.repository.UserImageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class UserImageService {

    private final UserImageRepository repository;

    @Value("${file.upload.user-images-dir}")
    private String uploadDir;

    private static final List<String> ALLOWED_TYPES = Arrays.asList("image/jpeg", "image/png");
    private static final long MAX_SIZE_BYTES = 20 * 1024 * 1024L;

    public UserImageService(UserImageRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public UserImageResponse upload(MultipartFile file, String view) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("JPG 또는 PNG 파일만 업로드할 수 있습니다.");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("파일 크기는 20MB를 초과할 수 없습니다.");
        }
        if (view == null || view.isBlank()) view = "front";
        if (!"front".equals(view)) {
            throw new IllegalArgumentException("현재는 정면(front) 사진만 업로드할 수 있습니다.");
        }

        String imageId = "usrimg_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String extension = "image/jpeg".equals(contentType) ? ".jpg" : ".png";
        String savedFilename = imageId + extension;

        Path dirPath = Paths.get(uploadDir);
        Files.createDirectories(dirPath);
        Files.copy(file.getInputStream(), dirPath.resolve(savedFilename),
                StandardCopyOption.REPLACE_EXISTING);

        UserImage entity = new UserImage();
        entity.setImageId(imageId);
        entity.setStatus("uploaded");
        entity.setView(view);
        entity.setFilename(file.getOriginalFilename());
        entity.setContentType(contentType);
        entity.setFileUrl("/files/user-images/" + savedFilename);

        repository.save(entity);
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public UserImageResponse getById(String imageId) {
        UserImage entity = repository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 이미지를 찾을 수 없습니다: " + imageId));
        return toResponse(entity);
    }

    @Transactional
    public void softDelete(String imageId) {
        UserImage image = repository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "이미지를 찾을 수 없습니다: " + imageId));
        image.setStatus("DELETED");
        repository.save(image);
    }

    private UserImageResponse toResponse(UserImage e) {
        return new UserImageResponse(
                e.getImageId(), e.getStatus(), e.getView(),
                e.getFilename(), e.getContentType(),
                e.getFileUrl(), e.getCreatedAt()
        );
    }
}