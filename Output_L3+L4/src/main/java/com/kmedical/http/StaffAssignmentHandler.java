package com.kmedical.http;

import com.kmedical.control.StaffAssignmentController;
import com.kmedical.domain.enums.StaffRole;
import com.kmedical.dto.staff.StaffAssignmentCreateRequestDTO;
import com.kmedical.dto.staff.StaffAssignmentDTO;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.*;

/**
 * SRV-C10 StaffAssignmentController HTTP 매핑
 *
 * POST /api/assignments                      스태프 배정
 * GET  /api/assignments?scheduleItemId=      일정 항목별 배정 목록
 * GET  /api/assignments?staffId=             스태프별 배정 목록
 */
public class StaffAssignmentHandler extends BaseHandler {

    private final StaffAssignmentController staffAssignmentController;

    public StaffAssignmentHandler(StaffAssignmentController staffAssignmentController) {
        this.staffAssignmentController = staffAssignmentController;
    }

    @Override
    protected void dispatch(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path   = ex.getRequestURI().getPath();

        if ("POST".equals(method) && "/api/assignments".equals(path)) {
            handleAssign(ex);
        } else if ("GET".equals(method) && "/api/assignments".equals(path)) {
            handleList(ex);
        } else {
            sendError(ex, 404, "Not Found: " + method + " " + path);
        }
    }

    private void handleAssign(HttpExchange ex) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));
        StaffAssignmentCreateRequestDTO req = new StaffAssignmentCreateRequestDTO();
        req.setScheduleItemId(body.get("scheduleItemId"));
        req.setStaffId(body.get("staffId"));
        if (body.get("staffRole") != null) req.setStaffRole(StaffRole.valueOf(body.get("staffRole")));
        req.setAssignedBy(body.get("assignedBy"));
        sendJson(ex, 201, assignmentToMap(staffAssignmentController.assignStaff(req)));
    }

    private void handleList(HttpExchange ex) throws IOException {
        String scheduleItemId = queryParam(ex, "scheduleItemId");
        String staffId        = queryParam(ex, "staffId");

        List<StaffAssignmentDTO> list;
        if (scheduleItemId != null) {
            list = staffAssignmentController.getAssignmentsByScheduleItem(scheduleItemId);
        } else if (staffId != null) {
            list = staffAssignmentController.getAssignmentsByStaff(staffId);
        } else {
            sendError(ex, 400, "Query parameter 'scheduleItemId' or 'staffId' is required."); return;
        }

        List<Map<String, Object>> items = new ArrayList<>();
        for (StaffAssignmentDTO a : list) items.add(assignmentToMap(a));
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("count", items.size()); resp.put("items", items);
        sendJson(ex, 200, resp);
    }

    private Map<String, Object> assignmentToMap(StaffAssignmentDTO dto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("assignmentId",        dto.getAssignmentId());
        m.put("scheduleItemId",      dto.getScheduleItemId());
        m.put("staffId",             dto.getStaffId());
        m.put("staffRole",           dto.getStaffRole() != null ? dto.getStaffRole().name() : null);
        m.put("assignedBy",          dto.getAssignedBy());
        m.put("assignedAt",          dto.getAssignedAt() != null ? dto.getAssignedAt().toString() : null);
        m.put("notificationSentAt",  dto.getNotificationSentAt() != null ? dto.getNotificationSentAt().toString() : null);
        return m;
    }
}
