package com.kmedical.control;

import com.kmedical.adapter.PushAdapter;
import com.kmedical.domain.entity.WorkProofPhoto;
import com.kmedical.domain.entity.WorkStatusUpdate;
import com.kmedical.dto.staff.WorkProofPhotoDTO;
import com.kmedical.dto.staff.WorkStatusUpdateDTO;
import com.kmedical.util.AuditLogger;
import com.kmedical.util.MaskingUtil;
import com.kmedical.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SRV-C11 — WorkController
 * 책임: 상태 변경 기록, 사진 업로드, 타임라인 동기화.
 * UC: UC-S05, UC-S06, UC-E03
 * NFR 적용: ConcurrentHashMap/CopyOnWriteArrayList, takenAt 과거 검증, fileUrl HTTPS 검증
 */
public class WorkController {

    private final PushAdapter     pushAdapter;
    private final AlertController alertController;
    private final Map<String, WorkStatusUpdate>    statusUpdateStore     = new ConcurrentHashMap<>();
    private final Map<String, List<WorkProofPhoto>> photoStore           = new ConcurrentHashMap<>();
    private final Map<String, List<String>>         journeyAssignmentIndex = new ConcurrentHashMap<>();

    public WorkController(PushAdapter pushAdapter, AlertController alertController) {
        this.pushAdapter     = pushAdapter;
        this.alertController = alertController;
    }

    private void guardNotClosedDown() {
        if (SystemStateRegistry.getInstance().isClosedDown()) {
            AuditLogger.closedDownAccess("WorkController", "UNKNOWN");
            throw new IllegalStateException("System is closed down. Customer operations are not permitted.");
        }
    }

    /**
     * 실무자의 업무 상태를 변경하고 기록한다.
     * 검증: assignmentId, staffId, newStatus not null
     */
    public WorkStatusUpdateDTO updateWorkStatus(WorkStatusUpdateDTO request) {
        guardNotClosedDown();
        ValidationUtil.requireNotNull(request, "WorkStatusUpdateDTO");
        ValidationUtil.requireNotBlank(request.getAssignmentId(), "assignmentId");
        ValidationUtil.requireNotBlank(request.getStaffId(), "staffId");
        ValidationUtil.requireNotNull(request.getNewStatus(), "newStatus");

        WorkStatusUpdate update = new WorkStatusUpdate();
        update.setWorkStatusUpdateId(UUID.randomUUID().toString());
        update.setAssignmentId(request.getAssignmentId());
        update.setStaffId(request.getStaffId());
        update.setNewStatus(request.getNewStatus());
        update.setChangedAt(LocalDateTime.now());
        update.setSyncedAt(LocalDateTime.now());

        statusUpdateStore.put(update.getWorkStatusUpdateId(), update);
        alertController.notifyWorkStatusChange(request.getStaffId(), request.getNewStatus().name());

        WorkStatusUpdateDTO result = new WorkStatusUpdateDTO();
        result.setAssignmentId(update.getAssignmentId());
        result.setStaffId(update.getStaffId());
        result.setNewStatus(update.getNewStatus());
        result.setChangedAt(update.getChangedAt());
        return result;
    }

    /**
     * 현장 증빙 사진을 업로드한다.
     * 검증: fileUrl HTTPS 필수, takenAt 과거 시각만 허용
     * 자동 설정: retentionExpiresAt = uploadedAt + 1년
     */
    public WorkProofPhotoDTO uploadProofPhoto(WorkProofPhotoDTO request) {
        guardNotClosedDown();
        ValidationUtil.requireNotNull(request, "WorkProofPhotoDTO");
        ValidationUtil.requireNotBlank(request.getAssignmentId(), "assignmentId");
        ValidationUtil.requireNotBlank(request.getStaffId(), "staffId");
        ValidationUtil.requireHttpsUrl(request.getFileUrl(), "fileUrl");

        // takenAt: 미래 시각 금지
        if (request.getTakenAt() != null) {
            ValidationUtil.requirePastDateTime(request.getTakenAt(), "takenAt");
        }

        LocalDateTime now = LocalDateTime.now();
        WorkProofPhoto photo = new WorkProofPhoto();
        photo.setWorkProofPhotoId(UUID.randomUUID().toString());
        photo.setAssignmentId(request.getAssignmentId());
        photo.setStaffId(request.getStaffId());
        photo.setFileUrl(request.getFileUrl());
        photo.setTakenAt(request.getTakenAt() != null ? request.getTakenAt() : now);
        photo.setUploadedAt(now);
        photo.setRetentionExpiresAt(now.plusYears(1));

        photoStore.computeIfAbsent(request.getAssignmentId(), k -> new CopyOnWriteArrayList<>()).add(photo);
        AuditLogger.log("WORK_PHOTO_UPLOAD", request.getStaffId(),
                MaskingUtil.maskUrl(request.getFileUrl()), true,
                "assignmentId=" + request.getAssignmentId());

        if (request.getPatientJourneyId() != null) {
            journeyAssignmentIndex
                    .computeIfAbsent(request.getPatientJourneyId(), k -> new CopyOnWriteArrayList<>())
                    .add(request.getAssignmentId());
        }

        WorkProofPhotoDTO result = new WorkProofPhotoDTO();
        result.setAssignmentId(photo.getAssignmentId());
        result.setPatientJourneyId(request.getPatientJourneyId());
        result.setStaffId(photo.getStaffId());
        result.setFileUrl(photo.getFileUrl());
        result.setTakenAt(photo.getTakenAt());
        return result;
    }

    /**
     * Invoice ISSUED 이벤트 수신 시 해당 여정의 모든 WorkProofPhoto 보관 기간 갱신.
     * retentionExpiresAt = Invoice.issuedAt + 1년 (제약#8)
     */
    public void extendRetentionOnInvoiceIssued(String patientJourneyId, LocalDateTime invoiceIssuedAt) {
        List<String> assignmentIds = journeyAssignmentIndex.getOrDefault(patientJourneyId, new ArrayList<>());
        for (String assignmentId : assignmentIds) {
            for (WorkProofPhoto photo : photoStore.getOrDefault(assignmentId, new ArrayList<>())) {
                photo.setRetentionExpiresAt(invoiceIssuedAt.plusYears(1));
            }
        }
    }

    /**
     * 배정별 상태 변경 이력을 조회한다.
     */
    public List<WorkStatusUpdateDTO> getStatusHistory(String assignmentId) {
        guardNotClosedDown();
        ValidationUtil.requireNotBlank(assignmentId, "assignmentId");
        List<WorkStatusUpdateDTO> result = new ArrayList<>();
        for (WorkStatusUpdate u : statusUpdateStore.values()) {
            if (assignmentId.equals(u.getAssignmentId())) {
                WorkStatusUpdateDTO dto = new WorkStatusUpdateDTO();
                dto.setAssignmentId(u.getAssignmentId());
                dto.setStaffId(u.getStaffId());
                dto.setNewStatus(u.getNewStatus());
                dto.setChangedAt(u.getChangedAt());
                result.add(dto);
            }
        }
        return result;
    }
}
