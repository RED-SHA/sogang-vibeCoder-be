package com.k.medtour.domain.file.dto;

import java.time.LocalDateTime;

public record FileDownloadResponse(
        Long fileId,
        String fileName,
        String downloadUrl,
        LocalDateTime expiresAt
) {
}
