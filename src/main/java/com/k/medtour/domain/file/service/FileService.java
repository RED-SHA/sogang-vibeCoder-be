package com.k.medtour.domain.file.service;

import com.k.medtour.domain.file.dto.FileDownloadResponse;
import com.k.medtour.domain.file.dto.FileUploadResponse;
import com.k.medtour.domain.file.entity.FileEntity;
import com.k.medtour.domain.file.enums.FileCategory;
import com.k.medtour.domain.file.repository.FileRepository;
import com.k.medtour.global.exception.BusinessException;
import com.k.medtour.global.exception.ErrorCode;
import com.k.medtour.infra.s3.StorageService;
import com.k.medtour.infra.s3.StorageService.StorageUploadResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileService {

    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024; // 20MB
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private final FileRepository fileRepository;
    private final StorageService storageService;

    @Transactional
    public FileUploadResponse upload(MultipartFile file, String categoryStr, Long uploaderId) {
        validateFileSize(file);
        validateMimeType(file);
        FileCategory category = parseCategory(categoryStr);

        String originalName = file.getOriginalFilename();
        String extension = extractExtension(originalName);
        String storedName = UUID.randomUUID() + extension;

        StorageUploadResult result = storageService.upload(file, storedName, category.name());

        FileEntity fileEntity = FileEntity.builder()
                .uploaderId(uploaderId)
                .originalName(originalName)
                .storedName(storedName)
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .category(category)
                .s3Key(result.s3Key())
                .url(result.url())
                .build();

        FileEntity saved = fileRepository.save(fileEntity);
        log.info("File uploaded: id={}, name={}, category={}, uploaderId={}",
                saved.getId(), originalName, category, uploaderId);

        return FileUploadResponse.from(saved);
    }

    public FileDownloadResponse getDownloadUrl(Long fileId) {
        FileEntity fileEntity = findFileOrThrow(fileId);

        String downloadUrl = storageService.generatePresignedUrl(fileEntity.getS3Key());
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

        return new FileDownloadResponse(
                fileEntity.getId(),
                fileEntity.getOriginalName(),
                downloadUrl,
                expiresAt
        );
    }

    @Transactional
    public void delete(Long fileId, Long memberId, String role) {
        FileEntity fileEntity = findFileOrThrow(fileId);

        if (!isAdminRole(role) && !fileEntity.getUploaderId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FILE_ACCESS_DENIED);
        }

        fileEntity.softDelete();
        storageService.delete(fileEntity.getS3Key());
        log.info("File deleted: id={}, by memberId={}", fileId, memberId);
    }

    private FileEntity findFileOrThrow(Long fileId) {
        return fileRepository.findByIdAndDeletedAtIsNull(fileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));
    }

    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
    }

    private void validateMimeType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    private FileCategory parseCategory(String categoryStr) {
        try {
            return FileCategory.valueOf(categoryStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_FILE_CATEGORY);
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private boolean isAdminRole(String role) {
        return "ADMIN".equalsIgnoreCase(role) || "MASTER".equalsIgnoreCase(role);
    }
}
