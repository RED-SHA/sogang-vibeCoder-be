package com.kmedical.control;

import com.kmedical.adapter.PushAdapter;
import com.kmedical.domain.entity.WorkProofPhoto;
import com.kmedical.domain.entity.WorkStatusUpdate;
import com.kmedical.dto.staff.WorkProofPhotoDTO;
import com.kmedical.dto.staff.WorkStatusUpdateDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SRV-C11 — WorkController
 * 책임: 상태 변경 기록, 사진 업로드, 타임라인 동기화.
 * UC: UC-S05, UC-S06, UC-E03
 */
public class WorkController {

    private final PushAdapter pushAdapter;
    private final AlertController alertController;
    private final Map<String, WorkStatusUpdate> statusUpdateStore = new HashMap<>();
    /** assignmentId → 증빙 사진 목록 */
    private final Map<String, List<WorkProofPhoto>> photoStore = new HashMap<>();
    /** patientJourneyId → assignmentId 목록 (Invoice ISSUED 시 보관 갱신을 위한 역방향 인덱스) */
    private final Map<String, List<String>> journeyAssignmentIndex = new HashMap<>();

    public WorkController(PushAdapter pushAdapter, AlertController alertController) {
        this.pushAdapter = pushAdapter;
        this.alertController = alertController;
    }

    private void guardNotClosedDown() {
        if (SystemStateRegistry.getInstance().isClosedDown()) {
            throw new IllegalStateException("System is closed down. Customer operations are not permitted.");
        }
    }

    /**
     * 실무자의 업무 상태를 변경하고 기록한다.
     * System Response: WorkStatusUpdate 저장 → AlertController 동기화 알림 위임 (UC-E03)
     */
    public WorkStatusUpdateDTO updateWorkStatus(WorkStatusUpdateDTO request) {
        guardNotClosedDown();
        if (request == null || request.getAssignmentId() == null || request.getStaffId() == null) {
            throw new IllegalArgumentException("WorkStatusUpdate request is incomplete.");
        }

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
     * System Response: 입력 검증 → WorkProofPhoto 저장 (retentionExpiresAt 초기값 설정)
     */
    public WorkProofPhotoDTO uploadProofPhoto(WorkProofPhotoDTO request) {
        guardNotClosedDown();
        if (request == null || request.getAssignmentId() == null || request.getFileUrl() == null) {
            throw new IllegalArgumentException("WorkProofPhoto upload request is incomplete.");
        }

        WorkProofPhoto photo = new WorkProofPhoto();
        photo.setWorkProofPhotoId(UUID.randomUUID().toString());
        photo.setAssignmentId(request.getAssignmentId());
        photo.setStaffId(request.getStaffId());
        photo.setFileUrl(request.getFileUrl());
        photo.setTakenAt(request.getTakenAt() != null ? request.getTakenAt() : LocalDateTime.now());
        photo.setUploadedAt(LocalDateTime.now());
        photo.setRetentionExpiresAt(LocalDateTime.now().plusYears(1));

        photoStore.computeIfAbsent(request.getAssignmentId(), k -> new ArrayList<>()).add(photo);

        // 여정-배정 역방향 인덱스 등록 (Invoice ISSUED 시 보관 갱신 지원)
        if (request.getPatientJourneyId() != null) {
            journeyAssignmentIndex
                    .computeIfAbsent(request.getPatientJourneyId(), k -> new ArrayList<>())
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
     * Invoice ISSUED 이벤트 수신 시 해당 여정의 모든 WorkProofPhoto 보관 기간을 갱신한다.
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
        List<WorkStatusUpdateDTO> result = new ArrayList<>();
        for (WorkStatusUpdate u : statusUpdateStore.values()) {
            if (u.getAssignmentId().equals(assignmentId)) {
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
