package com.kmedical.util;

import java.time.LocalDateTime;

/**
 * 감사 로그·경고·성능 로그 표준 출력 유틸리티 (NFR-LOG-01, NFR-PERF-01~03)
 * 출력 대상: System.out (콘솔)
 */
public final class AuditLogger {

    private AuditLogger() {}

    /**
     * 감사 로그 출력 (NFR-LOG-01)
     * 형식: [AUDIT] {ISO8601} | ACTION={} | ACTOR={} | TARGET={} | RESULT={SUCCESS|FAIL} | DETAIL={}
     */
    public static void log(String action, String actor, String target, boolean success, String detail) {
        System.out.printf("[AUDIT] %s | ACTION=%s | ACTOR=%s | TARGET=%s | RESULT=%s | DETAIL=%s%n",
                LocalDateTime.now(),
                action,
                actor  != null ? actor  : "UNKNOWN",
                target != null ? target : "-",
                success ? "SUCCESS" : "FAIL",
                detail != null ? sanitize(detail) : "");
    }

    /**
     * 경고 로그 출력
     * 형식: [WARN] {ISO8601} | {category} | {detail}
     */
    public static void warn(String category, String detail) {
        System.out.printf("[WARN] %s | %s | %s%n",
                LocalDateTime.now(),
                category,
                detail != null ? detail : "");
    }

    /**
     * 성능 측정 로그 출력 (NFR-PERF-01~03)
     * 형식: [PERF] {ISO8601} | ACTION={} | elapsed={}ms | threshold={}ms
     * 임계값 초과 시 [WARN] 추가 출력
     */
    public static void perf(String action, long elapsedMs, long thresholdMs) {
        System.out.printf("[PERF] %s | ACTION=%s | elapsed=%dms | threshold=%dms%n",
                LocalDateTime.now(), action, elapsedMs, thresholdMs);
        if (elapsedMs > thresholdMs) {
            System.out.printf("[WARN] %s | PERF_DEGRADED | ACTION=%s | elapsed=%dms exceeded threshold=%dms%n",
                    LocalDateTime.now(), action, elapsedMs, thresholdMs);
        }
    }

    /**
     * ClosedDown 상태 접근 시도 경고 (NFR-LOG-02)
     * 형식: [WARN] {ISO8601} | CLOSED_DOWN_ACCESS | METHOD={} | ACTOR={}
     */
    public static void closedDownAccess(String methodName, String actor) {
        System.out.printf("[WARN] %s | CLOSED_DOWN_ACCESS | METHOD=%s | ACTOR=%s%n",
                LocalDateTime.now(),
                methodName,
                actor != null ? actor : "UNKNOWN");
    }

    /** 예외 메시지에서 개인정보 패턴 제거 (로그 안전 출력) */
    private static String sanitize(String message) {
        if (message == null) return "";
        return message.replaceAll("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}", "[EMAIL]")
                      .replaceAll("\\+[1-9]\\d{6,14}", "[PHONE]");
    }
}
