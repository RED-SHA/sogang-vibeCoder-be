package com.kmedical.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * 입력값 제약 검증 유틸리티 — 위반 시 IllegalArgumentException
 */
public final class ValidationUtil {

    private static final Pattern EMAIL    = Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern UUID_V4  = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");
    private static final Pattern E164     = Pattern.compile("^\\+[1-9]\\d{6,14}$");
    private static final Pattern ISO2     = Pattern.compile("^[A-Z]{2}$");
    private static final Pattern PASSPORT = Pattern.compile("^[A-Z][A-Z0-9]{8}$");
    private static final Pattern NAME_EN  = Pattern.compile("^[A-Za-z\\s\\-']+$");
    private static final Pattern LICENSE  = Pattern.compile("^[A-Z0-9\\-]{5,20}$");

    private ValidationUtil() {}

    public static void requireNotNull(Object value, String fieldName) {
        if (value == null)
            throw new IllegalArgumentException(fieldName + " must not be null.");
    }

    public static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty())
            throw new IllegalArgumentException(fieldName + " must not be blank.");
    }

    public static void requireMaxLength(String value, int maxLen, String fieldName) {
        if (value != null && value.length() > maxLen)
            throw new IllegalArgumentException(fieldName + " must not exceed " + maxLen + " characters.");
    }

    public static void requireLengthBetween(String value, int minLen, int maxLen, String fieldName) {
        requireNotBlank(value, fieldName);
        if (value.trim().length() < minLen || value.length() > maxLen)
            throw new IllegalArgumentException(fieldName + " length must be between " + minLen + " and " + maxLen + ".");
    }

    public static void requireValidEmail(String email) {
        requireNotBlank(email, "email");
        requireMaxLength(email, 320, "email");
        if (!EMAIL.matcher(email).matches())
            throw new IllegalArgumentException("email must be a valid RFC 5321 address.");
    }

    public static void requireValidUUID(String id, String fieldName) {
        requireNotBlank(id, fieldName);
        if (!UUID_V4.matcher(id).matches())
            throw new IllegalArgumentException(fieldName + " must be a valid UUID v4.");
    }

    public static void requireValidE164Phone(String phone) {
        requireNotBlank(phone, "phoneE164");
        if (!E164.matcher(phone).matches())
            throw new IllegalArgumentException("phoneE164 must be E.164 format (e.g. +821012345678).");
    }

    public static void requireHttpsUrl(String url, String fieldName) {
        requireNotBlank(url, fieldName);
        if (!url.startsWith("https://"))
            throw new IllegalArgumentException(fieldName + " must use HTTPS scheme (must start with 'https://').");
    }

    public static void requireNonNegativeBigDecimal(BigDecimal val, String fieldName) {
        if (val != null && val.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException(fieldName + " must not be negative.");
    }

    public static void requirePositiveBigDecimal(BigDecimal val, String fieldName) {
        requireNotNull(val, fieldName);
        if (val.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException(fieldName + " must be greater than 0.");
    }

    public static void requireValidIso2Country(String code) {
        requireNotBlank(code, "nationality");
        if (!ISO2.matcher(code).matches())
            throw new IllegalArgumentException("nationality must be ISO 3166-1 alpha-2 uppercase (e.g. KR, US).");
    }

    public static void requireValidPassportNumber(String num, String fieldName) {
        if (num != null && !num.isEmpty() && !PASSPORT.matcher(num).matches())
            throw new IllegalArgumentException(fieldName + " must match ICAO passport number format (e.g. A12345678).");
    }

    public static void requireValidFullNameEn(String name, String fieldName) {
        requireLengthBetween(name, 2, 100, fieldName);
        if (!NAME_EN.matcher(name).matches())
            throw new IllegalArgumentException(fieldName + " must contain only English letters, spaces, hyphens, or apostrophes.");
    }

    public static void requireValidLicenseNumber(String licenseNumber) {
        requireNotBlank(licenseNumber, "licenseNumber");
        if (!LICENSE.matcher(licenseNumber).matches())
            throw new IllegalArgumentException("licenseNumber must be 5–20 uppercase alphanumeric/hyphen characters.");
    }

    public static void requireFutureDate(LocalDate date, String fieldName) {
        requireNotNull(date, fieldName);
        if (!date.isAfter(LocalDate.now()))
            throw new IllegalArgumentException(fieldName + " must be a future date (strictly after today).");
    }

    public static void requirePastOrPresentDate(LocalDate date, String fieldName) {
        requireNotNull(date, fieldName);
        if (date.isAfter(LocalDate.now()))
            throw new IllegalArgumentException(fieldName + " must not be a future date.");
        if (date.isBefore(LocalDate.of(1900, 1, 1)))
            throw new IllegalArgumentException(fieldName + " must be 1900-01-01 or later.");
    }

    public static void requirePastDateTime(LocalDateTime dt, String fieldName) {
        requireNotNull(dt, fieldName);
        if (!dt.isBefore(LocalDateTime.now()))
            throw new IllegalArgumentException(fieldName + " must be in the past (not a future or current instant).");
    }

    public static void requireTodayDate(LocalDate date, String fieldName) {
        requireNotNull(date, fieldName);
        if (!date.equals(LocalDate.now()))
            throw new IllegalArgumentException(fieldName + " must be today's date.");
    }

    public static void requireLatitude(BigDecimal lat) {
        requireNotNull(lat, "locationLat");
        if (lat.compareTo(new BigDecimal("-90")) < 0 || lat.compareTo(new BigDecimal("90")) > 0)
            throw new IllegalArgumentException("locationLat must be between -90 and 90.");
    }

    public static void requireLongitude(BigDecimal lng) {
        requireNotNull(lng, "locationLng");
        if (lng.compareTo(new BigDecimal("-180")) < 0 || lng.compareTo(new BigDecimal("180")) > 0)
            throw new IllegalArgumentException("locationLng must be between -180 and 180.");
    }

    public static void requireEndAfterStart(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && !end.isAfter(start))
            throw new IllegalArgumentException("scheduledEndAt must be after scheduledStartAt.");
    }
}
