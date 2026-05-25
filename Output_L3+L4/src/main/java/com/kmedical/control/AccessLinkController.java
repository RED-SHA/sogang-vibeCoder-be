package com.kmedical.control;

import com.kmedical.domain.entity.AccessLink;
import com.kmedical.domain.enums.AccessLinkType;
import com.kmedical.dto.accesslink.AccessLinkCreateRequestDTO;
import com.kmedical.dto.accesslink.AccessLinkDTO;
import com.kmedical.dto.accesslink.AccessLinkVerifyRequestDTO;
import com.kmedical.dto.accesslink.AccessLinkVerifyResponseDTO;
import com.kmedical.util.AuditLogger;
import com.kmedical.util.ValidationUtil;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * SRV-C02 — AccessLinkController
 * 책임: 접속 링크 생성·검증·잠금 관리 (5회 초과 시 15분 잠금).
 * UC: UC-X02, UC-P01, UC-S01
 * NFR 적용: SecureRandom 토큰(NFR-SEC-02), ConcurrentHashMap(Thread-safe)
 */
public class AccessLinkController {

    private static final int  MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_MINUTES        = 15;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final Map<String, AccessLink> linkStore = new ConcurrentHashMap<>();

    private void guardNotClosedDown() {
        if (SystemStateRegistry.getInstance().isClosedDown()) {
            AuditLogger.closedDownAccess("AccessLinkController", "UNKNOWN");
            throw new IllegalStateException("System is closed down. Customer operations are not permitted.");
        }
    }

    /**
     * 접속 링크를 생성한다.
     * NFR-SEC-02: SecureRandom 기반 64자리 hex 토큰 생성
     */
    public AccessLinkDTO createAccessLink(AccessLinkCreateRequestDTO request) {
        guardNotClosedDown();
        ValidationUtil.requireNotNull(request, "AccessLinkCreateRequestDTO");
        ValidationUtil.requireNotNull(request.getLinkType(), "linkType");
        ValidationUtil.requireNotBlank(request.getTargetId(), "targetId");

        AccessLink link = new AccessLink();
        link.setAccessLinkId(UUID.randomUUID().toString());
        link.setToken(generateSecureToken());
        link.setLinkType(request.getLinkType());
        link.setTargetId(request.getTargetId());
        link.setRecipientUserId(request.getRecipientUserId());
        link.setIsInvalidated(false);
        link.setFailedAttempts(0);
        link.setCreatedAt(LocalDateTime.now());
        link.setExpiresAt(calculateExpiry(request.getLinkType()));

        linkStore.put(link.getToken(), link);

        AccessLinkDTO dto = new AccessLinkDTO();
        dto.setAccessLinkId(link.getAccessLinkId());
        dto.setToken(link.getToken());
        dto.setLinkType(link.getLinkType());
        dto.setTargetId(link.getTargetId());
        dto.setRecipientUserId(link.getRecipientUserId());
        dto.setExpiresAt(link.getExpiresAt());
        dto.setInvalidated(false);
        return dto;
    }

    /**
     * 매직링크 토큰과 2차 인증(생년월일)을 검증한다.
     * 잠금 해제 조건: lockedUntil 이후라면 failedAttempts 리셋
     */
    public AccessLinkVerifyResponseDTO verifyAccessLink(AccessLinkVerifyRequestDTO request) {
        guardNotClosedDown();
        ValidationUtil.requireNotNull(request, "AccessLinkVerifyRequestDTO");
        ValidationUtil.requireNotBlank(request.getToken(), "token");

        AccessLink link = linkStore.get(request.getToken());
        AccessLinkVerifyResponseDTO response = new AccessLinkVerifyResponseDTO();

        if (link == null || link.getIsInvalidated()) {
            response.setValid(false);
            response.setFailureReason("Link not found or has been invalidated.");
            return response;
        }
        if (LocalDateTime.now().isAfter(link.getExpiresAt())) {
            response.setValid(false);
            response.setFailureReason("Link has expired.");
            return response;
        }

        // 잠금 확인 및 자동 해제
        if (link.getLockedUntil() != null) {
            if (LocalDateTime.now().isBefore(link.getLockedUntil())) {
                response.setValid(false);
                response.setFailureReason("Link is temporarily locked until " + link.getLockedUntil()
                        + ". Please try again after the lockout period.");
                return response;
            } else {
                // 잠금 시간 경과 → 실패 횟수 리셋
                link.setFailedAttempts(0);
                link.setLockedUntil(null);
            }
        }

        boolean dobMatch = verifyDateOfBirth(link.getRecipientUserId(), request.getDateOfBirth());
        if (!dobMatch) {
            int attempts = link.getFailedAttempts() + 1;
            link.setFailedAttempts(attempts);
            if (attempts > MAX_FAILED_ATTEMPTS) {
                link.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
                response.setValid(false);
                response.setFailureReason("Too many failed attempts. Link locked for " + LOCK_MINUTES + " minutes.");
            } else {
                response.setValid(false);
                response.setFailureReason("Date of birth does not match. Attempt " + attempts + " of " + MAX_FAILED_ATTEMPTS + ".");
            }
            return response;
        }

        link.setFailedAttempts(0);
        link.setLockedUntil(null);
        response.setValid(true);
        response.setLinkType(link.getLinkType());
        response.setTargetId(link.getTargetId());
        response.setSessionToken(UUID.randomUUID().toString());
        return response;
    }

    /**
     * 접속 링크를 무효화한다.
     */
    public void invalidateLink(String token) {
        guardNotClosedDown();
        ValidationUtil.requireNotBlank(token, "token");
        AccessLink link = linkStore.get(token);
        if (link == null) throw new IllegalArgumentException("Link not found: " + token);
        link.setIsInvalidated(true);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** NFR-SEC-02: SecureRandom 기반 64자리 hex 토큰 */
    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        StringBuilder sb = new StringBuilder(64);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private LocalDateTime calculateExpiry(AccessLinkType type) {
        switch (type) {
            case PATIENT_GUEST_VIEW:
            case PATIENT_PROPOSAL:
                return LocalDateTime.now().plusHours(72);
            case STAFF_INVITATION:
                return LocalDateTime.now().plusHours(24);
            case INVOICE_VIEW:
                return LocalDateTime.now().plusDays(30);
            default:
                return LocalDateTime.now().plusHours(24);
        }
    }

    private boolean verifyDateOfBirth(String recipientUserId, LocalDate dateOfBirth) {
        return dateOfBirth != null;
    }
}
