package com.kmedical.http;

import com.kmedical.control.StaffController;
import com.kmedical.domain.enums.StaffAvailability;
import com.kmedical.dto.staff.StaffDTO;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.*;

/**
 * SRV-C09 StaffController HTTP 매핑
 *
 * GET /api/staff/{id}                스태프 프로필 조회
 * PUT /api/staff/{id}/profile        프로필 수정
 * PUT /api/staff/{id}/availability   가용 상태 변경
 * GET /api/staff?agencyId=           에이전시 소속 스태프 목록
 */
public class StaffHandler extends BaseHandler {

    private final StaffController staffController;

    public StaffHandler(StaffController staffController) {
        this.staffController = staffController;
    }

    @Override
    protected void dispatch(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path   = ex.getRequestURI().getPath();
        String[] parts = path.split("/");

        if ("GET".equals(method) && "/api/staff".equals(path)) {
            handleListByAgency(ex);
        } else if ("GET".equals(method) && parts.length == 4) {
            handleGet(ex, parts[3]);
        } else if ("PUT".equals(method) && parts.length == 5 && "profile".equals(parts[4])) {
            handleUpdateProfile(ex, parts[3]);
        } else if ("PUT".equals(method) && parts.length == 5 && "availability".equals(parts[4])) {
            handleUpdateAvailability(ex, parts[3]);
        } else {
            sendError(ex, 404, "Not Found: " + method + " " + path);
        }
    }

    private void handleGet(HttpExchange ex, String id) throws IOException {
        sendJson(ex, 200, staffToMap(staffController.getStaff(id)));
    }

    private void handleListByAgency(HttpExchange ex) throws IOException {
        String agencyId = queryParam(ex, "agencyId");
        if (agencyId == null) { sendError(ex, 400, "Query parameter 'agencyId' is required."); return; }
        List<StaffDTO> list = staffController.getStaffByAgency(agencyId);
        List<Map<String, Object>> items = new ArrayList<>();
        for (StaffDTO s : list) items.add(staffToMap(s));
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("count", items.size()); resp.put("items", items);
        sendJson(ex, 200, resp);
    }

    private void handleUpdateProfile(HttpExchange ex, String staffId) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));
        StaffDTO dto = new StaffDTO();
        dto.setUserId(staffId);
        dto.setDisplayNameEn(body.get("displayNameEn"));
        dto.setSpecialization(body.get("specialization"));
        dto.setProfilePhotoUrl(body.get("profilePhotoUrl"));
        if (body.get("experienceYears") != null) dto.setExperienceYears(Integer.parseInt(body.get("experienceYears")));
        dto.setVehicleNumber(body.get("vehicleNumber"));
        dto.setVehicleType(body.get("vehicleType"));
        sendJson(ex, 200, staffToMap(staffController.updateProfile(dto)));
    }

    private void handleUpdateAvailability(HttpExchange ex, String staffId) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));
        StaffAvailability status = StaffAvailability.valueOf(body.get("availabilityStatus"));
        sendJson(ex, 200, staffToMap(staffController.updateAvailability(staffId, status)));
    }

    private Map<String, Object> staffToMap(StaffDTO dto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("userId",              dto.getUserId());
        m.put("agencyId",            dto.getAgencyId());
        m.put("staffType",           dto.getStaffType());
        m.put("displayNameEn",       dto.getDisplayNameEn());
        m.put("specialization",      dto.getSpecialization());
        m.put("profilePhotoUrl",     dto.getProfilePhotoUrl());
        m.put("experienceYears",     dto.getExperienceYears());
        m.put("availabilityStatus",  dto.getAvailabilityStatus() != null ? dto.getAvailabilityStatus().name() : null);
        m.put("onboardingComplete",  dto.getOnboardingComplete());
        m.put("vehicleNumber",       dto.getVehicleNumber());
        m.put("vehicleType",         dto.getVehicleType());
        return m;
    }
}
