package com.kmedical.util;

/**
 * 민감 정보 로그 마스킹 유틸리티 (NFR-SEC-01)
 */
public final class MaskingUtil {

    private MaskingUtil() {}

    /**
     * "abc***@gmail.com" 형태로 이메일 마스킹
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "[MASKED]";
        int atIdx = email.indexOf('@');
        String local = email.substring(0, atIdx);
        String domain = email.substring(atIdx);
        if (local.length() <= 3) return "***" + domain;
        return local.substring(0, 3) + "***" + domain;
    }

    /**
     * "1234****" 형태로 OAuth subject ID 마스킹
     */
    public static String maskSubjectId(String subjectId) {
        if (subjectId == null || subjectId.length() < 4) return "****";
        return subjectId.substring(0, 4) + "****";
    }

    /**
     * "a1b2c3d4..." 형태로 토큰 마스킹
     */
    public static String maskToken(String token) {
        if (token == null || token.length() < 8) return "********";
        return token.substring(0, 8) + "...";
    }

    /**
     * URL 경로 부분을 "***"로 대체하여 마스킹
     */
    public static String maskUrl(String url) {
        if (url == null) return "[MASKED]";
        int slashIdx = url.indexOf("//");
        if (slashIdx < 0) return "[MASKED]";
        int nextSlash = url.indexOf('/', slashIdx + 2);
        if (nextSlash < 0) return url.substring(0, slashIdx + 2) + "***";
        String host = url.substring(slashIdx + 2, nextSlash);
        if (host.length() > 6) {
            host = host.substring(0, 3) + "***" + host.substring(host.length() - 3);
        }
        return "https://" + host + "/***";
    }

    /**
     * 완전 제거 — OAuth 인가 코드 등 절대 노출 금지 필드용
     */
    public static String redact() {
        return "[REDACTED]";
    }
}
