package com.capstone.garment.service;

import com.capstone.garment.dto.GarmentResponse;
import com.capstone.garment.dto.GarmentUpdateRequest;
import com.capstone.garment.entity.Garment;
import com.capstone.garment.repository.GarmentRepository;
import com.capstone.user.entity.User;
import com.capstone.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GarmentService {

    private final GarmentRepository repository;
    private final UserRepository userRepository;

    @Value("${file.upload.garments-dir}")
    private String uploadDir;

    private static final List<String> ALLOWED_TYPES = Arrays.asList("image/jpeg", "image/png");
    private static final long MAX_SIZE_BYTES = 20 * 1024 * 1024L;
    private static final List<String> ALLOWED_CATEGORIES = Arrays.asList(
            "top", "bottom", "dress", "outer", "shoes", "bag"
    );

    public GarmentService(GarmentRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Transactional
    public GarmentResponse upload(MultipartFile file, String category, String email) throws IOException {

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("JPG 또는 PNG 파일만 업로드할 수 있습니다.");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("파일 크기는 20MB를 초과할 수 없습니다.");
        }
        if (category != null && !category.isBlank() && !ALLOWED_CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("유효하지 않은 카테고리입니다. 허용: " + ALLOWED_CATEGORIES);
        }

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String garmentId = "gar_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String extension = "image/jpeg".equals(contentType) ? ".jpg" : ".png";
        String savedFilename = garmentId + extension;

        Path dirPath = Paths.get(uploadDir);
        Files.createDirectories(dirPath);
        Files.copy(file.getInputStream(), dirPath.resolve(savedFilename),
                StandardCopyOption.REPLACE_EXISTING);

        Garment entity = new Garment();
        entity.setGarmentId(garmentId);
        entity.setOwnerUserId(owner.getId());
        entity.setStatus("ACTIVE");
        entity.setSourceType("UPLOAD");
        entity.setCategory(category);
        entity.setFilename(file.getOriginalFilename());
        entity.setContentType(contentType);
        entity.setFileUrl("/files/garments/" + savedFilename);

        repository.save(entity);
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<GarmentResponse> list(String q, String category, String sourceType, String brandKey) {
        return repository.searchGarments(
                        blankToNull(q),
                        blankToNull(category),
                        blankToNull(sourceType),
                        blankToNull(brandKey)
                )
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // 빈 문자열은 null로 처리해 JPQL의 :param IS NULL 조건이 제대로 작동하도록
    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    @Transactional(readOnly = true)
    public GarmentResponse getById(String garmentId) {
        Garment entity = repository.findById(garmentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 의류를 찾을 수 없습니다: " + garmentId));
        return toResponse(entity);
    }

    @Transactional
    public GarmentResponse update(String garmentId, GarmentUpdateRequest request) {
        Garment entity = repository.findById(garmentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 의류를 찾을 수 없습니다: " + garmentId));

        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            entity.setCategory(request.getCategory());
        }
        if (request.getBrandKey() != null) {
            entity.setBrandKey(request.getBrandKey());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            entity.setStatus(request.getStatus());
        }

        repository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    public void softDelete(String garmentId) {
        Garment entity = repository.findById(garmentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 의류를 찾을 수 없습니다: " + garmentId));
        entity.setStatus("HIDDEN");
        repository.save(entity);
    }

    private GarmentResponse toResponse(Garment e) {
        return new GarmentResponse(
                String.valueOf(e.getGarmentId()),
                e.getStatus(),
                e.getSourceType(),
                e.getCategory(),
                e.getName(),
                e.getPrice(),
                e.getContentType(),
                e.getFileUrl(),
                e.getBrandKey(),
                e.getCreatedAt()
        );
    }
}