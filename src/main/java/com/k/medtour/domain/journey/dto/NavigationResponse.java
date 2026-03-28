package com.k.medtour.domain.journey.dto;

import java.util.Map;

public record NavigationResponse(
        DestinationDto destination,
        Map<String, String> deepLinks
) {
    public record DestinationDto(
            String name,
            Double latitude,
            Double longitude
    ) {
    }
}
