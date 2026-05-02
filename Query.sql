-- =============================================================
-- 캡스톤 가상피팅 서비스 - MariaDB 초기화 스키마
-- 변경: 29CM API 의존 테이블 전면 제거 → 자체 카탈로그 구조
-- =============================================================

SET NAMES utf8mb4;
SET time_zone = '+09:00';

-- -------------------------------------------------------------
-- 1. users
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `users` (
                                       `id`                BIGINT          NOT NULL AUTO_INCREMENT,
                                       `email`             VARCHAR(255)    NOT NULL,
                                       `password_hash`     VARCHAR(255)    NOT NULL,
                                       `nickname`          VARCHAR(50)     NOT NULL,
                                       `profile_image_url` VARCHAR(500)    NULL,
                                       `role`              ENUM('USER','ADMIN','SELLER') NOT NULL DEFAULT 'USER',
                                       `status`            ENUM('ACTIVE','INACTIVE','BLOCKED') NOT NULL DEFAULT 'ACTIVE',
                                       `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                       `deleted_at`        DATETIME        NULL,
                                       PRIMARY KEY (`id`),
                                       UNIQUE KEY `uq_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 2. categories  (자체 카테고리 트리 — 29CM 종속 없음)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `categories` (
                                            `code`          VARCHAR(50)     NOT NULL,
                                            `parent_code`   VARCHAR(50)     NULL,
                                            `name`          VARCHAR(255)    NOT NULL,
                                            `depth`         TINYINT         NOT NULL DEFAULT 0,
                                            `is_leaf`       BOOLEAN         NOT NULL DEFAULT FALSE,
                                            `sort_order`    INT             NOT NULL DEFAULT 0,
                                            `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                            PRIMARY KEY (`code`),
                                            KEY `idx_categories_parent` (`parent_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 3. brands  (자체 브랜드 테이블 — cm29_brands 대체)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `brands` (
                                        `id`            BIGINT          NOT NULL AUTO_INCREMENT,
                                        `brand_key`     VARCHAR(100)    NOT NULL,
                                        `brand_name`    VARCHAR(255)    NOT NULL,
                                        `brand_name_ko` VARCHAR(255)    NULL,
                                        `logo_url`      VARCHAR(500)    NULL,
                                        `status`        ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
                                        `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                        PRIMARY KEY (`id`),
                                        UNIQUE KEY `uq_brands_key` (`brand_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 4. garments  (의류 아이템 — 29CM 필드 전부 제거)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `garments` (
                                          `id`                BIGINT          NOT NULL AUTO_INCREMENT,
                                          `owner_user_id`     BIGINT          NULL,
                                          `brand_id`          BIGINT          NULL,
                                          `category_code`     VARCHAR(50)     NULL,
                                          `name`              VARCHAR(255)    NOT NULL,
                                          `description`       TEXT            NULL,
                                          `brand_name`        VARCHAR(255)    NULL,
                                          `price`             DECIMAL(10,2)   NULL,
                                          `currency`          VARCHAR(10)     NOT NULL DEFAULT 'KRW',
                                          `file_url`          VARCHAR(500)    NOT NULL,
                                          `thumbnail_url`     VARCHAR(500)    NULL,
                                          `mask_url`          VARCHAR(500)    NULL,
                                          `source_type`       ENUM('UPLOAD','EXTERNAL_LINK') NOT NULL DEFAULT 'UPLOAD',
                                          `status`            ENUM('ACTIVE','HIDDEN','DELETED') NOT NULL DEFAULT 'ACTIVE',
                                          `metadata_json`     JSON            NULL,
                                          `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                          `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                          `deleted_at`        DATETIME        NULL,
                                          PRIMARY KEY (`id`),
                                          KEY `idx_garments_owner`    (`owner_user_id`),
                                          KEY `idx_garments_brand`    (`brand_id`),
                                          KEY `idx_garments_category` (`category_code`),
                                          KEY `idx_garments_status`   (`status`),
                                          FULLTEXT KEY `ft_garments_name` (`name`, `brand_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 5. garment_images
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `garment_images` (
                                                `id`            BIGINT          NOT NULL AUTO_INCREMENT,
                                                `garment_id`    BIGINT          NOT NULL,
                                                `image_type`    ENUM('MAIN','DETAIL','MODEL','THUMBNAIL') NOT NULL DEFAULT 'MAIN',
                                                `image_url`     VARCHAR(500)    NOT NULL,
                                                `sort_order`    INT             NOT NULL DEFAULT 0,
                                                `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                PRIMARY KEY (`id`),
                                                KEY `idx_garment_images_garment_sort` (`garment_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 6. user_images  (사용자 신체 사진)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_images` (
                                             `id`                    BIGINT          NOT NULL AUTO_INCREMENT,
                                             `user_id`               BIGINT          NOT NULL,
                                             `original_file_name`    VARCHAR(255)    NOT NULL,
                                             `file_url`              VARCHAR(500)    NOT NULL,
                                             `file_path`             VARCHAR(500)    NULL,
                                             `mime_type`             VARCHAR(100)    NOT NULL,
                                             `file_size`             BIGINT          NOT NULL,
                                             `width`                 INT             NULL,
                                             `height`                INT             NULL,
                                             `view_type`             ENUM('FRONT','BACK','LEFT','RIGHT') NOT NULL DEFAULT 'FRONT',
                                             `status`                ENUM('UPLOADED','PROCESSING','READY','FAILED','DELETED') NOT NULL DEFAULT 'UPLOADED',
                                             `created_at`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             `updated_at`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                             `deleted_at`            DATETIME        NULL,
                                             PRIMARY KEY (`id`),
                                             KEY `idx_user_images_user_status`   (`user_id`, `status`),
                                             KEY `idx_user_images_created_at`    (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 7. tryons  (가상피팅 작업 큐 — polling 구조 유지, 29CM 필드 제거)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tryons` (
                                        `id`                BIGINT          NOT NULL AUTO_INCREMENT,
                                        `user_id`           BIGINT          NOT NULL,
                                        `user_image_id`     BIGINT          NOT NULL,
                                        `garment_id`        BIGINT          NOT NULL,
                                        `status`            ENUM('QUEUED','CLAIMED','PROCESSING','COMPLETED','FAILED','CANCELLED')
                                                                            NOT NULL DEFAULT 'QUEUED',
                                        `progress`          TINYINT         NOT NULL DEFAULT 0,
                                        `request_json`      JSON            NULL,
                                        `worker_token`      VARCHAR(100)    NULL,
                                        `error_message`     VARCHAR(500)    NULL,
                                        `requested_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        `claimed_at`        DATETIME        NULL,
                                        `started_at`        DATETIME        NULL,
                                        `completed_at`      DATETIME        NULL,
                                        `failed_at`         DATETIME        NULL,
                                        `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                        `deleted_at`        DATETIME        NULL,
                                        PRIMARY KEY (`id`),
                                        KEY `idx_tryons_user_created`   (`user_id`, `created_at`),
                                        KEY `idx_tryons_status_created` (`status`, `created_at`),
                                        KEY `idx_tryons_worker_status`  (`worker_token`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 8. results  (피팅 결과 이미지)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `results` (
                                         `id`                    BIGINT          NOT NULL AUTO_INCREMENT,
                                         `tryon_id`              BIGINT          NOT NULL,
                                         `user_id`               BIGINT          NOT NULL,
                                         `result_image_url`      VARCHAR(500)    NOT NULL,
                                         `result_thumbnail_url`  VARCHAR(500)    NULL,
                                         `storage_path`          VARCHAR(500)    NULL,
                                         `generation_ms`         INT             NULL,
                                         `created_at`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         `deleted_at`            DATETIME        NULL,
                                         PRIMARY KEY (`id`),
                                         UNIQUE KEY `uq_results_tryon`       (`tryon_id`),
                                         KEY `idx_results_user_created`      (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 9. result_feedbacks  (피팅 결과 평점/코멘트)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `result_feedbacks` (
                                                  `id`                    BIGINT          NOT NULL AUTO_INCREMENT,
                                                  `result_id`             BIGINT          NOT NULL,
                                                  `user_id`               BIGINT          NOT NULL,
                                                  `rating`                TINYINT         NOT NULL COMMENT '1~5 별점',
                                                  `comment`               VARCHAR(1000)   NULL,
                                                  `recommendation_mode`   ENUM('SIMILAR','CONTRAST','MIXED') NULL,
                                                  `created_at`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                  `updated_at`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                  PRIMARY KEY (`id`),
                                                  UNIQUE KEY `uq_feedback_result_user` (`result_id`, `user_id`),
                                                  KEY `idx_result_feedbacks_rating`   (`rating`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 10. favorites  (즐겨찾기)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `favorites` (
                                           `user_id`       BIGINT      NOT NULL,
                                           `garment_id`    BIGINT      NOT NULL,
                                           `created_at`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           PRIMARY KEY (`user_id`, `garment_id`),
                                           KEY `idx_favorites_user_created` (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 기본 카테고리 시드 데이터
-- -------------------------------------------------------------
INSERT IGNORE INTO `categories` (`code`,`parent_code`,`name`,`depth`,`is_leaf`,`sort_order`) VALUES
                                                                                                 ('TOP',    NULL,    '상의',    0, FALSE, 1),
                                                                                                 ('BOTTOM', NULL,    '하의',    0, FALSE, 2),
                                                                                                 ('OUTER',  NULL,    '아우터',  0, FALSE, 3),
                                                                                                 ('DRESS',  NULL,    '원피스',  0, TRUE,  4),
                                                                                                 ('TOP_TS', 'TOP',   '티셔츠',  1, TRUE,  1),
                                                                                                 ('TOP_SH', 'TOP',   '셔츠',    1, TRUE,  2),
                                                                                                 ('TOP_KN', 'TOP',   '니트',    1, TRUE,  3),
                                                                                                 ('TOP_HO', 'TOP',   '후드',    1, TRUE,  4),
                                                                                                 ('BOT_PT', 'BOTTOM','팬츠',    1, TRUE,  1),
                                                                                                 ('BOT_SK', 'BOTTOM','스커트',  1, TRUE,  2),
                                                                                                 ('BOT_SH', 'BOTTOM','반바지',  1, TRUE,  3),
                                                                                                 ('OUT_JK', 'OUTER', '재킷',    1, TRUE,  1),
                                                                                                 ('OUT_CT', 'OUTER', '코트',    1, TRUE,  2),
                                                                                                 ('OUT_PD', 'OUTER', '패딩',    1, TRUE,  3);

-- -------------------------------------------------------------
-- 테스트용 관리자 계정 (password: admin1234 — bcrypt)
-- -------------------------------------------------------------
INSERT IGNORE INTO `users`
(`email`, `password_hash`, `nickname`, `role`, `status`, `created_at`, `updated_at`)
VALUES (
           'admin@capstone.dev',
           '$2a$10$7EqJtq98hPqEX7fNZaFWoOhAs79vcMoFBdFBAWs.aKD/lSMF5NKIC',
           '관리자', 'ADMIN', 'ACTIVE', NOW(), NOW()
       );


select * from users;