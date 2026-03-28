package com.k.medtour.domain.profile.dto;

import java.util.List;

public record PortfolioResponse(
        Long organizationId,
        String name,
        String description,
        List<PortfolioItem> portfolioItems
) {
    public record PortfolioItem(
            Long fileId,
            String fileName,
            String fileUrl,
            String category,
            String description
    ) {
    }
}
