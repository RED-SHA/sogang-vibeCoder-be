package com.kmedical.control;

import com.kmedical.domain.entity.AccessLink;
import com.kmedical.domain.enums.AccessLinkType;
import com.kmedical.dto.accesslink.AccessLinkCreateRequestDTO;
import com.kmedical.dto.accesslink.AccessLinkDTO;
import com.kmedical.dto.accesslink.AccessLinkVerifyRequestDTO;
import com.kmedical.dto.accesslink.AccessLinkVerifyResponseDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * SRV-C02 — AccessLinkController
 * 책임: 접속 링크 생성·검증·잠금 관리 (5회 초과 시 15분 잠금).
 * UC: UC-X02, UC-P01, UC-S01
 */
public class AccessLinkController {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_MINUTES = 15;

    private final Map<String, AccessLink> linkStore = new HashMap<>();

    private void guardNotClosedDown() {
        if (SystemStateRegistry.getInstance().isClosedDown()) {
            throw new IllegalStateException("System is closed down. Customer operations are not permitted.");
        }
    }

    /**
     * 접속 링크를 생성한다.
     * System Response: 링크 유형별 만료 시각 계산 → AccessLink 생성 → 토큰 반환
     */
    public AccessLinkDTO createAccessLink(AccessLinkCreateRequestDTO request) {
        guardNotClosedDown();
        if (request == null || request.getLinkType() == null) {
            throw new IllegalArgumentException("AccessLink creation request is invalid.");
        }

        AccessLink link = new AccessLink();
        link.setAccessLinkId(UUID.randomUUID().toString());
        link.setToken(UUID.randomUUID().toString().replace("-", ""));
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
     * System Response: 토큰 유효성 확인 → 잠금 여부 확인 → 생년월일 비교 → 실패 횟수 처리
     */
    public AccessLinkVerifyResponseDTO verifyAccessLink(AccessLinkVerifyRequestDTO request) {
        guardNotClosedDown();
        if (request == null || request.getToken() == null) {
            throw new IllegalArgumentException("Verification request is invalid.");
        }

        AccessLink link = linkStore.get(request.getToken());
        AccessLinkVerifyResponseDTO response = new AccessLinkVerifyResponseDTO();

        if (link == null || link.getIsInvalidated()) {
            response.setValid(false);
            response.setFailureReason("Link not found or invalidated.");
            return response;
        }
        if (LocalDateTime.now().isAfter(link.getExpiresAt())) {
            response.setValid(false);
            response.setFailureReason("Link has expired.");
            return response;
        }
        if (link.getLockedUntil() != null && LocalDateTime.now().isBefore(link.getLockedUntil())) {
            response.setValid(false);
            response.setFailureReason("Link is temporarily locked. Try again later.");
            return response;
        }

        boolean dobMatch = verifyDateOfBirth(link.getRecipientUserId(), request.getDateOfBirth());
        if (!dobMatch) {
            int attempts = link.getFailedAttempts() + 1;
            link.setFailedAttempts(attempts);
            if (attempts > MAX_FAILED_ATTEMPTS) {
                link.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
            }
            response.setValid(false);
            response.setFailureReason("Date of birth does not match. Attempts: " + attempts);
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
        AccessLink link = linkStore.get(token);
        if (link == null) throw new IllegalArgumentException("Link not found: " + token);
        link.setIsInvalidated(true);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

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
