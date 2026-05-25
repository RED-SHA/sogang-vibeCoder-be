package com.kmedical.control;

import com.kmedical.domain.entity.EmergencyAlert;
import com.kmedical.domain.enums.AlertChannel;
import com.kmedical.domain.enums.AlertType;
import com.kmedical.dto.alert.AlertCreateRequestDTO;
import com.kmedical.dto.sos.SOSAlertDTO;
import com.kmedical.dto.sos.SOSAlertRequestDTO;
import com.kmedical.util.AuditLogger;
import com.kmedical.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SRV-C14 — SOSController
 * 책임: 긴급 호출 생성, 에스컬레이션, AlertController 연계.
 * UC: UC-S10, UC-E02
 * NFR 적용: ConcurrentHashMap, 좌표 범위 검증, PERF 로깅(500ms 임계값), AuditLogger
 */
public class SOSController {

    private static final long SOS_PERF_THRESHOLD_MS = 500L;

    private final AlertController alertController;
    private final Map<String, EmergencyAlert> sosStore = new ConcurrentHashMap<>();

    public SOSController(AlertController alertController) {
        this.alertController = alertController;
    }

    private void guardNotClosedDown() {
        if (SystemStateRegistry.getInstance().isClosedDown()) {
            AuditLogger.closedDownAccess("SOSController", "UNKNOWN");
            throw new IllegalStateException("System is closed down. Customer operations are not permitted.");
        }
    }

    /**
     * 긴급 호출을 생성하고 관리자에게 에스컬레이션한다.
     * 검증: staffId/patientJourneyId not null, 위·경도 범위(-90~90, -180~180)
     * NFR-PERF-01: 500ms 처리 시간 목표, 초과 시 [WARN] 로그
     * NFR-LOG-01: SOS_TRIGGERED 감사 로그
     */
    public SOSAlertDTO triggerSOS(SOSAlertRequestDTO request) {
        guardNotClosedDown();

        ValidationUtil.requireNotNull(request, "SOSAlertRequestDTO");
        ValidationUtil.requireNotBlank(request.getStaffId(), "staffId");
        ValidationUtil.requireNotBlank(request.getPatientJourneyId(), "patientJourneyId");
        if (request.getLocationLat() != null) ValidationUtil.requireLatitude(request.getLocationLat());
        if (request.getLocationLng() != null) ValidationUtil.requireLongitude(request.getLocationLng());
        ValidationUtil.requireMaxLength(request.getNotes(), 1000, "notes");

        long start = System.currentTimeMillis();
        try {
            EmergencyAlert alert = new EmergencyAlert();
            alert.setEmergencyAlertId(UUID.randomUUID().toString());
            alert.setStaffId(request.getStaffId());
            alert.setPatientJourneyId(request.getPatientJourneyId());
            alert.setLocationLat(request.getLocationLat());
            alert.setLocationLng(request.getLocationLng());
            alert.setNotes(request.getNotes());
            alert.setReportedAt(LocalDateTime.now());

            sosStore.put(alert.getEmergencyAlertId(), alert);

            String content = "SOS from staff " + request.getStaffId()
                    + " at journey " + request.getPatientJourneyId();
            alertController.sendAlert(new AlertCreateRequestDTO(AlertType.SOS_ALERT, AlertChannel.PUSH, request.getStaffId(), content));
            alertController.sendAlert(new AlertCreateRequestDTO(AlertType.SOS_ALERT, AlertChannel.WHATSAPP, request.getStaffId(), content));

            AuditLogger.log("SOS_TRIGGERED", request.getStaffId(), request.getPatientJourneyId(), true,
                    "alertId=" + alert.getEmergencyAlertId());
            return toDTO(alert);

        } finally {
            AuditLogger.perf("SOS_TRIGGER", System.currentTimeMillis() - start, SOS_PERF_THRESHOLD_MS);
        }
    }

    /**
     * 긴급 호출을 해결 처리한다.
     * NFR-LOG-01: SOS_RESOLVED 감사 로그
     */
    public SOSAlertDTO resolveSOS(String emergencyAlertId, String resolvedBy, String notes) {
        guardNotClosedDown();
        ValidationUtil.requireNotBlank(emergencyAlertId, "emergencyAlertId");
        ValidationUtil.requireNotBlank(resolvedBy, "resolvedBy");

        EmergencyAlert alert = findSOS(emergencyAlertId);
        try {
            if (alert.getResolvedAt() != null)
                throw new IllegalStateException("SOS alert is already resolved.");
            alert.setResolvedAt(LocalDateTime.now());
            alert.setResolvedBy(resolvedBy);
            if (notes != null) alert.setNotes(notes);

            AuditLogger.log("SOS_RESOLVED", resolvedBy, emergencyAlertId, true,
                    "resolvedAt=" + alert.getResolvedAt());
            return toDTO(alert);

        } catch (Exception e) {
            AuditLogger.log("SOS_RESOLVED", resolvedBy, emergencyAlertId, false, e.getMessage());
            if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) throw e;
            throw new IllegalStateException("SOS resolve failed: " + e.getMessage());
        }
    }

    /**
     * 미해결 긴급 호출 목록을 조회한다.
     */
    public List<SOSAlertDTO> getUnresolvedSOS() {
        List<SOSAlertDTO> result = new ArrayList<>();
        for (EmergencyAlert a : sosStore.values()) {
            if (a.getResolvedAt() == null) result.add(toDTO(a));
        }
        return result;
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private EmergencyAlert findSOS(String id) {
        EmergencyAlert a = sosStore.get(id);
        if (a == null) throw new IllegalArgumentException("EmergencyAlert not found: " + id);
        return a;
    }

    private SOSAlertDTO toDTO(EmergencyAlert a) {
        SOSAlertDTO dto = new SOSAlertDTO();
        dto.setEmergencyAlertId(a.getEmergencyAlertId());
        dto.setStaffId(a.getStaffId());
        dto.setPatientJourneyId(a.getPatientJourneyId());
        dto.setLocationLat(a.getLocationLat());
        dto.setLocationLng(a.getLocationLng());
        dto.setReportedAt(a.getReportedAt());
        dto.setResolvedAt(a.getResolvedAt());
        dto.setResolvedBy(a.getResolvedBy());
        dto.setNotes(a.getNotes());
        return dto;
    }
}
