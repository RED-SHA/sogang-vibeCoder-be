package com.kmedical.control;

import com.kmedical.domain.entity.EmergencyAlert;
import com.kmedical.domain.enums.AlertChannel;
import com.kmedical.domain.enums.AlertType;
import com.kmedical.dto.alert.AlertCreateRequestDTO;
import com.kmedical.dto.sos.SOSAlertDTO;
import com.kmedical.dto.sos.SOSAlertRequestDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SRV-C14 — SOSController
 * 책임: 긴급 호출 생성, 에스컬레이션, AlertController 연계.
 * UC: UC-S10, UC-E02
 */
public class SOSController {

    private final AlertController alertController;
    private final Map<String, EmergencyAlert> sosStore = new HashMap<>();

    public SOSController(AlertController alertController) {
        this.alertController = alertController;
    }

    private void guardNotClosedDown() {
        if (SystemStateRegistry.getInstance().isClosedDown()) {
            throw new IllegalStateException("System is closed down. Customer operations are not permitted.");
        }
    }

    /**
     * 긴급 호출을 생성하고 관리자에게 에스컬레이션한다.
     * System Response: EmergencyAlert 생성 → AlertController Push+WhatsApp 발송
     */
    public SOSAlertDTO triggerSOS(SOSAlertRequestDTO request) {
        guardNotClosedDown();
        if (request == null || request.getStaffId() == null) {
            throw new IllegalArgumentException("SOS alert request is incomplete.");
        }

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

        return toDTO(alert);
    }

    /**
     * 긴급 호출을 해결 처리한다.
     * System Response: resolvedAt·resolvedBy 갱신
     */
    public SOSAlertDTO resolveSOS(String emergencyAlertId, String resolvedBy, String notes) {
        guardNotClosedDown();
        EmergencyAlert alert = findSOS(emergencyAlertId);
        if (alert.getResolvedAt() != null) {
            throw new IllegalStateException("SOS alert is already resolved.");
        }
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolvedBy(resolvedBy);
        if (notes != null) alert.setNotes(notes);
        return toDTO(alert);
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
