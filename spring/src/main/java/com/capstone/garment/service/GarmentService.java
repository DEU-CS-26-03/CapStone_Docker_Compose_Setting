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
            "top", "bottom", "dress", "outer", "shoes", "bag", "upper", "lower", "overall"
    );

    public GarmentService(GarmentRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Transactional
    // 💡 파라미터에 String fileUrl 추가
    public GarmentResponse upload(MultipartFile file, String fileUrl, String category, String name, String brandName, String price, String email) throws IOException {

        if (category != null && !category.isBlank() && !ALLOWED_CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("유효하지 않은 카테고리입니다. 허용: " + ALLOWED_CATEGORIES);
        }

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String garmentId = "gar_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        String finalFileUrl = fileUrl; // 기본적으로 웹 URL이 들어왔다면 그대로 사용
        String contentType = null;
        String originalFilename = null;

        // ★ 1. 파일이 존재하는 경우 (로컬 파일 업로드)
        if (file != null && !file.isEmpty()) {
            contentType = file.getContentType();
            if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
                throw new IllegalArgumentException("JPG 또는 PNG 파일만 업로드할 수 있습니다.");
            }
            if (file.getSize() > MAX_SIZE_BYTES) {
                throw new IllegalArgumentException("파일 크기는 20MB를 초과할 수 없습니다.");
            }

            String extension = "image/jpeg".equals(contentType) ? ".jpg" : ".png";
            String savedFilename = garmentId + extension;
            originalFilename = file.getOriginalFilename();

            Path dirPath = Paths.get(uploadDir);
            Files.createDirectories(dirPath);
            Files.copy(file.getInputStream(), dirPath.resolve(savedFilename), StandardCopyOption.REPLACE_EXISTING);

            finalFileUrl = "/files/garments/" + savedFilename;
        }
        // ★ 2. 파일도 없고, 프론트에서 보낸 URL도 없는 경우 에러 처리
        else if (fileUrl == null || fileUrl.isBlank()) {
            throw new IllegalArgumentException("이미지 파일이나 유효한 웹 이미지 URL 중 하나는 반드시 제공해야 합니다.");
        }

        Garment entity = new Garment();
        entity.setGarmentId(garmentId);
        entity.setOwnerUserId(owner.getId());
        entity.setStatus("ACTIVE");
        entity.setSourceType("UPLOAD");
        entity.setCategory(category);

        entity.setName(name != null && !name.isBlank() ? name : "이름 없음");
        entity.setBrandKey(brandName != null && !brandName.isBlank() ? brandName : "기타");
        entity.setPrice(parsePrice(price));

        // 파일이 있을 때만 파일명과 컨텐츠 타입 지정 (URL만 올 경우엔 생략됨)
        if (originalFilename != null) entity.setFilename(originalFilename);
        if (contentType != null) entity.setContentType(contentType);

        // 최종 결정된 경로 (로컬 파일 경로 or 외부 웹 URL) 저장
        entity.setFileUrl(finalFileUrl);

        repository.save(entity);
        return toResponse(entity);
    }

    private Integer parsePrice(String priceStr) {
        if (priceStr == null || priceStr.isBlank()) return 0;
        try {
            return Integer.parseInt(priceStr.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) { return 0; }
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

    @Transactional(readOnly = true)
    public List<GarmentResponse> getRecommendations(String type, String category) {
        List<Garment> garments;
        if ("similar".equalsIgnoreCase(type)) {
            garments = repository.findSimilarGarments(category);
        } else {
            garments = repository.findDifferentGarments(category);
        }
        return garments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
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