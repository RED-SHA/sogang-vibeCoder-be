package com.kmedical.control;

import com.kmedical.domain.entity.GuideDeliveryHistory;
import com.kmedical.domain.entity.RecoveryGuide;
import com.kmedical.domain.enums.AlertChannel;
import com.kmedical.domain.enums.AlertType;
import com.kmedical.dto.alert.AlertCreateRequestDTO;
import com.kmedical.dto.guide.GuideCreateRequestDTO;
import com.kmedical.dto.guide.GuideDeliveryRequestDTO;
import com.kmedical.dto.guide.RecoveryGuideDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SRV-C16 — GuideController
 * 책임: 회복 가이드 등록, 배포, 열람 이력 관리.
 * UC: UC-A10, UC-P13
 */
public class GuideController {

    private final AlertController alertController;
    private final Map<String, RecoveryGuide> guideStore = new HashMap<>();
    private final Map<String, List<GuideDeliveryHistory>> deliveryStore = new HashMap<>();

    public GuideController(AlertController alertController) {
        this.alertController = alertController;
    }

    private void guardNotClosedDown() {
        if (SystemStateRegistry.getInstance().isClosedDown()) {
            throw new IllegalStateException("System is closed down. Customer operations are not permitted.");
        }
    }

    /**
     * 회복 가이드를 등록한다.
     * System Response: 입력 검증 → RecoveryGuide 저장 → DTO 반환
     */
    public RecoveryGuideDTO createGuide(GuideCreateRequestDTO request) {
        guardNotClosedDown();
        if (request == null || request.getAgencyId() == null) {
            throw new IllegalArgumentException("Guide creation request is incomplete.");
        }

        RecoveryGuide guide = new RecoveryGuide();
        guide.setRecoveryGuideId(UUID.randomUUID().toString());
        guide.setAgencyId(request.getAgencyId());
        guide.setSurgeryType(request.getSurgeryType());
        guide.setTitleEn(request.getTitleEn());
        guide.setContentEn(request.getContentEn());
        guide.setPdfUrl(request.getPdfUrl());
        guide.setCreatedAt(LocalDateTime.now());

        guideStore.put(guide.getRecoveryGuideId(), guide);
        return toDTO(guide);
    }

    /**
     * 회복 가이드를 환자에게 배포한다.
     * System Response: 가이드 존재 확인 → GuideDeliveryHistory 저장 → 알림 발송
     */
    public void deliverGuide(GuideDeliveryRequestDTO request) {
        guardNotClosedDown();
        if (request == null || request.getRecoveryGuideId() == null || request.getPatientId() == null) {
            throw new IllegalArgumentException("Guide delivery request is incomplete.");
        }

        RecoveryGuide guide = findGuide(request.getRecoveryGuideId());

        GuideDeliveryHistory history = new GuideDeliveryHistory();
        history.setGuideDeliveryHistoryId(UUID.randomUUID().toString());
        history.setRecoveryGuideId(guide.getRecoveryGuideId());
        history.setPatientId(request.getPatientId());
        history.setDeliveredBy(request.getDeliveredBy());
        history.setDeliveredAt(LocalDateTime.now());
        history.setIsVisible(true);

        deliveryStore.computeIfAbsent(request.getPatientId(), k -> new ArrayList<>()).add(history);

        alertController.sendAlert(new AlertCreateRequestDTO(
                AlertType.GUIDE_SHARED, AlertChannel.PUSH, request.getPatientId(),
                "A recovery guide has been shared with you: " + guide.getTitleEn()));
    }

    /**
     * 회복 가이드를 조회한다.
     */
    public RecoveryGuideDTO getGuide(String guideId) {
        guardNotClosedDown();
        return toDTO(findGuide(guideId));
    }

    /**
     * 환자에게 배포된 가이드 목록을 조회한다.
     */
    public List<RecoveryGuideDTO> getGuidesForPatient(String patientId) {
        guardNotClosedDown();
        List<RecoveryGuideDTO> result = new ArrayList<>();
        for (GuideDeliveryHistory h : deliveryStore.getOrDefault(patientId, new ArrayList<>())) {
            if (Boolean.TRUE.equals(h.getIsVisible())) {
                RecoveryGuide guide = guideStore.get(h.getRecoveryGuideId());
                if (guide != null) result.add(toDTO(guide));
            }
        }
        return result;
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private RecoveryGuide findGuide(String id) {
        RecoveryGuide g = guideStore.get(id);
        if (g == null) throw new IllegalArgumentException("RecoveryGuide not found: " + id);
        return g;
    }

    private RecoveryGuideDTO toDTO(RecoveryGuide g) {
        RecoveryGuideDTO dto = new RecoveryGuideDTO();
        dto.setRecoveryGuideId(g.getRecoveryGuideId());
        dto.setAgencyId(g.getAgencyId());
        dto.setSurgeryType(g.getSurgeryType());
        dto.setTitleEn(g.getTitleEn());
        dto.setContentEn(g.getContentEn());
        dto.setPdfUrl(g.getPdfUrl());
        dto.setCreatedAt(g.getCreatedAt());
        return dto;
    }
}
