//업로드·검증·목록·조회 로직
package com.capstone.garment.service;

import com.capstone.garment.dto.GarmentResponse;
import com.capstone.garment.entity.Garment;
import com.capstone.garment.repository.GarmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

    @Value("${file.upload.garments-dir}")
    private String uploadDir;

    // jpg, png만 허용
    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "image/jpeg", "image/png"
    );

    // yml: max-file-size: 20MB
    private static final long MAX_SIZE_BYTES = 20 * 1024 * 1024L;

    // 허용 카테고리 목록
    private static final List<String> ALLOWED_CATEGORIES = Arrays.asList(
            "top", "bottom", "dress", "outer", "shoes", "bag"
    );

    public GarmentService(GarmentRepository repository) {
        this.repository = repository;
    }

    /**
     * 의류 이미지 업로드
     * - jpg, png만 허용
     * - 10MB 초과 시 예외
     * - category: top / bottom / dress / outer / shoes / bag
     */
    public GarmentResponse upload(MultipartFile file, String category) throws IOException {

        // 파일 타입 검증
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("JPG 또는 PNG 파일만 업로드할 수 있습니다.");
        }

        // 용량 검증
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("파일 크기는 20MB를 초과할 수 없습니다.");
        }`

        // 카테고리 검증
        if (category != null && !category.isBlank()
                && !ALLOWED_CATEGORIES.contains(category)) {
            throw new IllegalArgumentException(
                    "유효하지 않은 카테고리입니다. 허용: " + ALLOWED_CATEGORIES);
        }

        // 저장 파일명 생성
        String garmentId = "gar_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String extension = "image/jpeg".equals(contentType) ? ".jpg" : ".png";
        String savedFilename = garmentId + extension;

        // 디렉토리 생성 및 저장
        Path dirPath = Paths.get(uploadDir);
        Files.createDirectories(dirPath);
        Files.copy(file.getInputStream(), dirPath.resolve(savedFilename),
                StandardCopyOption.REPLACE_EXISTING);

        // DB 저장
        Garment entity = new Garment();
        entity.setGarmentId(garmentId);
        entity.setStatus("uploaded");
        entity.setCategory(category);
        entity.setFilename(file.getOriginalFilename());
        entity.setContentType(contentType);
        entity.setFileUrl("/files/garments/" + savedFilename);

        repository.save(entity);

        return toResponse(entity);
    }

    /**
     * 의류 목록 조회
     * - category 파라미터 있으면 필터, 없으면 전체
     */
    public List<GarmentResponse> list(String category) {
        List<Garment> result = (category != null && !category.isBlank())
                ? repository.findByCategory(category)
                : repository.findAll();

        return result.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 의류 메타정보 단건 조회
     */
    public GarmentResponse getById(String garmentId) {
        Garment entity = repository.findById(garmentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 의류를 찾을 수 없습니다: " + garmentId));
        return toResponse(entity);
    }

    private GarmentResponse toResponse(Garment e) {
        return new GarmentResponse(
                e.getGarmentId(),
                e.getStatus(),
                e.getCategory(),
                e.getFilename(),
                e.getContentType(),
                e.getFileUrl(),
                e.getCreatedAt()
        );
    }
}