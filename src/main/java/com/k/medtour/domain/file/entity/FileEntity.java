package com.k.medtour.domain.file.entity;

import com.k.medtour.domain.file.enums.FileCategory;
import com.k.medtour.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "file_entity")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileEntity extends BaseEntity {

    @Column(name = "uploader_id", nullable = false)
    private Long uploaderId;

    @Column(name = "original_name", nullable = false, length = 500)
    private String originalName;

    @Column(name = "stored_name", nullable = false, length = 100)
    private String storedName;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private FileCategory category;

    @Column(name = "s3_key", nullable = false, length = 500)
    private String s3Key;

    @Column(name = "url", nullable = false, length = 1000)
    private String url;

    @Builder
    public FileEntity(Long uploaderId, String originalName, String storedName,
                      String mimeType, Long fileSize, FileCategory category,
                      String s3Key, String url) {
        this.uploaderId = uploaderId;
        this.originalName = originalName;
        this.storedName = storedName;
        this.mimeType = mimeType;
        this.fileSize = fileSize;
        this.category = category;
        this.s3Key = s3Key;
        this.url = url;
    }
}
