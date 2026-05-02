package com.capstone.storage.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "upload_sessions")
public class UploadSession {

    @Id
    private String token;

    private Long userId;
    private String objectKey;
    private String fileName;
    private String contentType;
    private String uploadType;
    private LocalDateTime expiresAt;
    private boolean uploaded;
    private LocalDateTime createdAt;
}