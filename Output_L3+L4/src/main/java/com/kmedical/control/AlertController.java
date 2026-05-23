package com.kmedical.control;

import com.kmedical.adapter.MessengerAdapter;
import com.kmedical.adapter.PushAdapter;
import com.kmedical.domain.entity.Alert;
import com.kmedical.domain.enums.AlertChannel;
import com.kmedical.domain.enums.AlertStatus;
import com.kmedical.domain.enums.AlertType;
import com.kmedical.dto.alert.AlertCreateRequestDTO;
import com.kmedical.dto.alert.AlertDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SRV-C13 — AlertController
 * 책임: 알림 생성 및 채널 발송 (Push/WhatsApp/Email).
 * UC: UC-N01, UC-E01, UC-E03
 */
public class AlertController {

    private final PushAdapter pushAdapter;
    private final MessengerAdapter messengerAdapter;
    private final Map<String, Alert> alertStore = new HashMap<>();

    public AlertController(PushAdapter pushAdapter, MessengerAdapter messengerAdapter) {
        this.pushAdapter = pushAdapter;
        this.messengerAdapter = messengerAdapter;
    }

    private void guardNotClosedDown() {
        if (SystemStateRegistry.getInstance().isClosedDown()) {
            throw new IllegalStateException("System is closed down. Customer operations are not permitted.");
        }
    }

    /**
     * 알림을 생성하고 지정 채널로 발송한다.
     * System Response: Alert 생성(PENDING) → 채널 발송 → 상태 갱신(SENT/FAILED)
     */
    public AlertDTO sendAlert(AlertCreateRequestDTO request) {
        guardNotClosedDown();
        if (request == null || request.getRecipientUserId() == null) {
            throw new IllegalArgumentException("Alert request is incomplete.");
        }

        Alert alert = new Alert();
        alert.setAlertId(UUID.randomUUID().toString());
        alert.setAlertType(request.getAlertType());
        alert.setChannel(request.getChannel());
        alert.setRecipientUserId(request.getRecipientUserId());
        alert.setContent(request.getContent());
        alert.setStatus(AlertStatus.PENDING);

        boolean success = dispatch(alert);
        alert.setStatus(success ? AlertStatus.SENT : AlertStatus.FAILED);
        alert.setSentAt(LocalDateTime.now());

        alertStore.put(alert.getAlertId(), alert);
        return toDTO(alert);
    }

    /** UC-E01 — 일정 변경 Push 알림 (JourneyController에서 위임). */
    public void notifyScheduleChange(String scheduleItemId) {
        guardNotClosedDown();
        AlertCreateRequestDTO req = new AlertCreateRequestDTO(
                AlertType.SCHEDULE_CHANGE,
                AlertChannel.PUSH,
                scheduleItemId,
                "A critical schedule item has been updated."
        );
        sendAlert(req);
    }

    /** UC-E03 — 작업 상태 변경 동기화 알림 (WorkController에서 위임). */
    public void notifyWorkStatusChange(String staffId, String newStatus) {
        guardNotClosedDown();
        AlertCreateRequestDTO req = new AlertCreateRequestDTO(
                AlertType.STAFF_ASSIGNED,
                AlertChannel.PUSH,
                staffId,
                "Work status updated to: " + newStatus
        );
        sendAlert(req);
    }

    /**
     * 사용자의 알림 이력을 조회한다.
     */
    public List<AlertDTO> getAlertsForUser(String recipientUserId) {
        guardNotClosedDown();
        List<AlertDTO> result = new ArrayList<>();
        for (Alert a : alertStore.values()) {
            if (a.getRecipientUserId().equals(recipientUserId)) result.add(toDTO(a));
        }
        return result;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private boolean dispatch(Alert alert) {
        try {
            if (alert.getChannel() == AlertChannel.PUSH) {
                return pushAdapter.sendPush(alert.getRecipientUserId(), alert.getAlertType().name(), alert.getContent());
            } else if (alert.getChannel() == AlertChannel.WHATSAPP) {
                return messengerAdapter.sendWhatsApp(alert.getRecipientUserId(), alert.getContent());
            } else if (alert.getChannel() == AlertChannel.EMAIL) {
                return messengerAdapter.sendEmail(alert.getRecipientUserId(), alert.getAlertType().name(), alert.getContent());
            }
        } catch (Exception e) {
            alert.setFailureReason(e.getMessage());
        }
        return false;
    }

    private AlertDTO toDTO(Alert a) {
        AlertDTO dto = new AlertDTO();
        dto.setAlertId(a.getAlertId());
        dto.setAlertType(a.getAlertType());
        dto.setChannel(a.getChannel());
        dto.setRecipientUserId(a.getRecipientUserId());
        dto.setContent(a.getContent());
        dto.setStatus(a.getStatus());
        dto.setSentAt(a.getSentAt());
        dto.setFailureReason(a.getFailureReason());
        return dto;
    }
}
