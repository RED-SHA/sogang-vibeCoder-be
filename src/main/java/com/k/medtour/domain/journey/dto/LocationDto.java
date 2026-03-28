package com.k.medtour.domain.journey.dto;

import java.util.Map;

public record LocationDto(
        String name,
        String address,
        Double latitude,
        Double longitude,
        String googleMapsUrl
) {
    public static LocationDto from(Map<String, Object> location) {
        if (location == null) return null;
        String name = (String) location.get("name");
        String address = (String) location.get("address");
        Double lat = toDouble(location.get("latitude"));
        Double lng = toDouble(location.get("longitude"));
        String googleMapsUrl = (lat != null && lng != null)
                ? "https://maps.google.com/?q=" + lat + "," + lng
                : null;
        return new LocationDto(name, address, lat, lng, googleMapsUrl);
    }

    public Map<String, Object> toMap() {
        return Map.of(
                "name", name != null ? name : "",
                "address", address != null ? address : "",
                "latitude", latitude != null ? latitude : 0.0,
                "longitude", longitude != null ? longitude : 0.0
        );
    }

    private static Double toDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Double d) return d;
        if (value instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
