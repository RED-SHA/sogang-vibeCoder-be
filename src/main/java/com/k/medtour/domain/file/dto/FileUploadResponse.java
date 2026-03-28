package com.k.medtour.domain.file.dto;

import com.k.medtour.domain.file.entity.FileEntity;
import com.k.medtour.domain.file.enums.FileCategory;

import java.time.LocalDateTime;

public record FileUploadResponse(
        Long id,
        String fileName,
        Long fileSize,
        String mimeType,
        FileCategory category,
        String url,
        LocalDateTime uploadedAt
) {
    public static FileUploadResponse from(FileEntity entity) {
        return new FileUploadResponse(
                entity.getId(),
                entity.getOriginalName(),
                entity.getFileSize(),
                entity.getMimeType(),
                entity.getCategory(),
                entity.getUrl(),
                entity.getCreatedAt()
        );
    }
}
