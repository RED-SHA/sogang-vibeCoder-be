package com.kmedical.http;

import com.kmedical.control.TemplateController;
import com.kmedical.domain.enums.ScheduleItemType;
import com.kmedical.domain.enums.StaffRole;
import com.kmedical.dto.template.*;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.*;

/**
 * SRV-C07 TemplateController HTTP 매핑
 *
 * POST /api/templates              여정 템플릿 생성
 * PUT  /api/templates              여정 템플릿 수정
 * GET  /api/templates/{id}         템플릿 단건 조회
 * GET  /api/templates?agencyId=    에이전시 템플릿 목록
 * POST /api/templates/{id}/items   템플릿 항목 추가
 * GET  /api/templates/{id}/items   템플릿 항목 목록
 */
public class TemplateHandler extends BaseHandler {

    private final TemplateController templateController;

    public TemplateHandler(TemplateController templateController) {
        this.templateController = templateController;
    }

    @Override
    protected void dispatch(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path   = ex.getRequestURI().getPath();
        String[] parts = path.split("/");

        if ("POST".equals(method) && "/api/templates".equals(path)) {
            handleCreate(ex);
        } else if ("PUT".equals(method) && "/api/templates".equals(path)) {
            handleUpdate(ex);
        } else if ("GET".equals(method) && "/api/templates".equals(path)) {
            handleListByAgency(ex);
        } else if ("GET".equals(method) && parts.length == 4) {
            handleGet(ex, parts[3]);
        } else if ("POST".equals(method) && parts.length == 5 && "items".equals(parts[4])) {
            handleAddItem(ex, parts[3]);
        } else if ("GET".equals(method) && parts.length == 5 && "items".equals(parts[4])) {
            handleGetItems(ex, parts[3]);
        } else {
            sendError(ex, 404, "Not Found: " + method + " " + path);
        }
    }

    private void handleCreate(HttpExchange ex) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));
        ItineraryTemplateDTO dto = new ItineraryTemplateDTO();
        dto.setAgencyId(body.get("agencyId"));
        dto.setProductType(body.get("productType"));
        dto.setCreatedBy(body.get("createdBy"));
        if (body.get("durationDays") != null) dto.setDurationDays(Integer.parseInt(body.get("durationDays")));
        sendJson(ex, 201, templateToMap(templateController.createTemplate(dto)));
    }

    private void handleUpdate(HttpExchange ex) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));
        ItineraryTemplateDTO dto = new ItineraryTemplateDTO();
        dto.setItineraryTemplateId(body.get("itineraryTemplateId"));
        dto.setProductType(body.get("productType"));
        if (body.get("durationDays") != null) dto.setDurationDays(Integer.parseInt(body.get("durationDays")));
        sendJson(ex, 200, templateToMap(templateController.updateTemplate(dto)));
    }

    private void handleGet(HttpExchange ex, String id) throws IOException {
        sendJson(ex, 200, templateToMap(templateController.getTemplate(id)));
    }

    private void handleListByAgency(HttpExchange ex) throws IOException {
        String agencyId = queryParam(ex, "agencyId");
        if (agencyId == null) { sendError(ex, 400, "Query parameter 'agencyId' is required."); return; }
        List<ItineraryTemplateDTO> list = templateController.getTemplatesByAgency(agencyId);
        List<Map<String, Object>> items = new ArrayList<>();
        for (ItineraryTemplateDTO t : list) items.add(templateToMap(t));
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("count", items.size()); resp.put("items", items);
        sendJson(ex, 200, resp);
    }

    private void handleAddItem(HttpExchange ex, String templateId) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));
        TemplateItemDTO dto = new TemplateItemDTO();
        dto.setItineraryTemplateId(templateId);
        dto.setTitle(body.get("title"));
        if (body.get("itemType") != null) dto.setItemType(ScheduleItemType.valueOf(body.get("itemType")));
        if (body.get("assignedStaffRole") != null) dto.setAssignedStaffRole(StaffRole.valueOf(body.get("assignedStaffRole")));
        if (body.get("durationMinutes") != null) dto.setDurationMinutes(Integer.parseInt(body.get("durationMinutes")));
        if (body.get("sortOrder") != null) dto.setSortOrder(Integer.parseInt(body.get("sortOrder")));
        sendJson(ex, 201, itemToMap(templateController.addTemplateItem(dto)));
    }

    private void handleGetItems(HttpExchange ex, String templateId) throws IOException {
        List<TemplateItemDTO> list = templateController.getTemplateItems(templateId);
        List<Map<String, Object>> items = new ArrayList<>();
        for (TemplateItemDTO i : list) items.add(itemToMap(i));
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("count", items.size()); resp.put("items", items);
        sendJson(ex, 200, resp);
    }

    private Map<String, Object> templateToMap(ItineraryTemplateDTO dto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("itineraryTemplateId", dto.getItineraryTemplateId());
        m.put("agencyId",            dto.getAgencyId());
        m.put("productType",         dto.getProductType());
        m.put("durationDays",        dto.getDurationDays());
        m.put("createdBy",           dto.getCreatedBy());
        m.put("createdAt",           dto.getCreatedAt() != null ? dto.getCreatedAt().toString() : null);
        m.put("updatedAt",           dto.getUpdatedAt() != null ? dto.getUpdatedAt().toString() : null);
        return m;
    }

    private Map<String, Object> itemToMap(TemplateItemDTO dto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("templateItemId",      dto.getTemplateItemId());
        m.put("itineraryTemplateId", dto.getItineraryTemplateId());
        m.put("itemType",            dto.getItemType() != null ? dto.getItemType().name() : null);
        m.put("title",               dto.getTitle());
        m.put("durationMinutes",     dto.getDurationMinutes());
        m.put("assignedStaffRole",   dto.getAssignedStaffRole() != null ? dto.getAssignedStaffRole().name() : null);
        m.put("sortOrder",           dto.getSortOrder());
        return m;
    }
}
