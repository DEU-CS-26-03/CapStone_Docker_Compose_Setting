package com.capstone.garment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "garments")
@Getter
@Setter
public class Garment {

    @Id
    @Column(name = "garment_id")
    private String garmentId;

    @Column(name = "owner_user_id")
    private Long ownerUserId;

    @Column(name = "source_type")
    private String sourceType;          // UPLOAD | 29CM_IMPORT | EXTERNAL_LINK

    @Column(name = "external_item_key")
    private String externalItemKey;     // 29CM import 원본 키

    @Column(name = "partner_brand_key")
    private String partnerBrandKey;

    @Column(name = "standard_category_code")
    private String standardCategoryCode;

    @Column(name = "name")
    private String name;                // 29CM 상품명 또는 파일명 대체

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "brand_key")
    private String brandKey;

    @Column(name = "category")
    private String category;            // top | bottom | dress | outer | shoes | bag

    @Column(name = "filename")
    private String filename;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "mask_url")
    private String maskUrl;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "status")
    private String status;              // ACTIVE | HIDDEN | DELETED

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}