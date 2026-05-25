package com.kmedical.util;

import java.time.LocalDateTime;
import java.util.logging.Logger;

/**
 * 감사 로그·경고·성능 로그 표준 출력 유틸리티 (NFR-LOG-01, NFR-PERF-01~03)
 * 출력 대상: java.util.logging (JDK 표준 Logger)
 */
public final class AuditLogger {

    private static final Logger LOGGER = Logger.getLogger(AuditLogger.class.getName());

    private AuditLogger() {}

    /**
     * 감사 로그 출력 (NFR-LOG-01)
     * 형식: [AUDIT] {ISO8601} | ACTION={} | ACTOR={} | TARGET={} | RESULT={SUCCESS|FAIL} | DETAIL={}
     */
    public static void log(String action, String actor, String target, boolean success, String detail) {
        LOGGER.info(String.format("[AUDIT] %s | ACTION=%s | ACTOR=%s | TARGET=%s | RESULT=%s | DETAIL=%s",
                LocalDateTime.now(),
                action,
                actor  != null ? actor  : "UNKNOWN",
                target != null ? target : "-",
                success ? "SUCCESS" : "FAIL",
                detail != null ? sanitize(detail) : ""));
    }

    /**
     * 경고 로그 출력
     * 형식: [WARN] {ISO8601} | {category} | {detail}
     */
    public static void warn(String category, String detail) {
        LOGGER.warning(String.format("[WARN] %s | %s | %s",
                LocalDateTime.now(),
                category,
                detail != null ? detail : ""));
    }

    /**
     * 성능 측정 로그 출력 (NFR-PERF-01~03)
     * 형식: [PERF] {ISO8601} | ACTION={} | elapsed={}ms | threshold={}ms
     * 임계값 초과 시 [WARN] 추가 출력
     */
    public static void perf(String action, long elapsedMs, long thresholdMs) {
        LOGGER.info(String.format("[PERF] %s | ACTION=%s | elapsed=%dms | threshold=%dms",
                LocalDateTime.now(), action, elapsedMs, thresholdMs));
        if (elapsedMs > thresholdMs) {
            LOGGER.warning(String.format("[WARN] %s | PERF_DEGRADED | ACTION=%s | elapsed=%dms exceeded threshold=%dms",
                    LocalDateTime.now(), action, elapsedMs, thresholdMs));
        }
    }

    /**
     * ClosedDown 상태 접근 시도 경고 (NFR-LOG-02)
     * 형식: [WARN] {ISO8601} | CLOSED_DOWN_ACCESS | METHOD={} | ACTOR={}
     */
    public static void closedDownAccess(String methodName, String actor) {
        LOGGER.warning(String.format("[WARN] %s | CLOSED_DOWN_ACCESS | METHOD=%s | ACTOR=%s",
                LocalDateTime.now(),
                methodName,
                actor != null ? actor : "UNKNOWN"));
    }

    /** 예외 메시지에서 개인정보 패턴 제거 (로그 안전 출력) */
    private static String sanitize(String message) {
        if (message == null) return "";
        return message.replaceAll("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}", "[EMAIL]")
                      .replaceAll("\\+[1-9]\\d{6,14}", "[PHONE]");
    }
}
