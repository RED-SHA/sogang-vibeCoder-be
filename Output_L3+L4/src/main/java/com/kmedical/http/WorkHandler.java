package com.kmedical.http;

import com.kmedical.control.WorkController;
import com.kmedical.domain.enums.WorkStatus;
import com.kmedical.dto.staff.WorkProofPhotoDTO;
import com.kmedical.dto.staff.WorkStatusUpdateDTO;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * SRV-C11 WorkController HTTP 매핑
 *
 * POST /api/work/status               업무 상태 변경 기록
 * POST /api/work/photos               증빙 사진 업로드
 * GET  /api/work/history?assignmentId= 상태 변경 이력 조회
 */
public class WorkHandler extends BaseHandler {

    private final WorkController workController;

    public WorkHandler(WorkController workController) {
        this.workController = workController;
    }

    @Override
    protected void dispatch(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path   = ex.getRequestURI().getPath();

        if ("POST".equals(method) && "/api/work/status".equals(path)) {
            handleUpdateStatus(ex);
        } else if ("POST".equals(method) && "/api/work/photos".equals(path)) {
            handleUploadPhoto(ex);
        } else if ("GET".equals(method) && "/api/work/history".equals(path)) {
            handleGetHistory(ex);
        } else {
            sendError(ex, 404, "Not Found: " + method + " " + path);
        }
    }

    private void handleUpdateStatus(HttpExchange ex) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));
        WorkStatusUpdateDTO dto = new WorkStatusUpdateDTO();
        dto.setAssignmentId(body.get("assignmentId"));
        dto.setStaffId(body.get("staffId"));
        if (body.get("newStatus") != null) dto.setNewStatus(WorkStatus.valueOf(body.get("newStatus")));
        WorkStatusUpdateDTO result = workController.updateWorkStatus(dto);
        sendJson(ex, 200, statusToMap(result));
    }

    private void handleUploadPhoto(HttpExchange ex) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));
        WorkProofPhotoDTO dto = new WorkProofPhotoDTO();
        dto.setAssignmentId(body.get("assignmentId"));
        dto.setPatientJourneyId(body.get("patientJourneyId"));
        dto.setStaffId(body.get("staffId"));
        dto.setFileUrl(body.get("fileUrl"));
        if (body.get("takenAt") != null) dto.setTakenAt(LocalDateTime.parse(body.get("takenAt")));
        WorkProofPhotoDTO result = workController.uploadProofPhoto(dto);
        sendJson(ex, 201, photoToMap(result));
    }

    private void handleGetHistory(HttpExchange ex) throws IOException {
        String assignmentId = queryParam(ex, "assignmentId");
        if (assignmentId == null) { sendError(ex, 400, "Query parameter 'assignmentId' is required."); return; }
        List<WorkStatusUpdateDTO> list = workController.getStatusHistory(assignmentId);
        List<Map<String, Object>> items = new ArrayList<>();
        for (WorkStatusUpdateDTO u : list) items.add(statusToMap(u));
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("count", items.size()); resp.put("items", items);
        sendJson(ex, 200, resp);
    }

    private Map<String, Object> statusToMap(WorkStatusUpdateDTO dto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("assignmentId", dto.getAssignmentId());
        m.put("staffId",      dto.getStaffId());
        m.put("newStatus",    dto.getNewStatus() != null ? dto.getNewStatus().name() : null);
        m.put("changedAt",    dto.getChangedAt() != null ? dto.getChangedAt().toString() : null);
        return m;
    }

    private Map<String, Object> photoToMap(WorkProofPhotoDTO dto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("assignmentId",     dto.getAssignmentId());
        m.put("patientJourneyId", dto.getPatientJourneyId());
        m.put("staffId",          dto.getStaffId());
        m.put("fileUrl",          dto.getFileUrl());
        m.put("takenAt",          dto.getTakenAt() != null ? dto.getTakenAt().toString() : null);
        return m;
    }
}
