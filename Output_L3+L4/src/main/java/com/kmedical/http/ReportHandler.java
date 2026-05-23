package com.kmedical.http;

import com.kmedical.control.ReportController;
import com.kmedical.dto.staff.DailyWorkReportDTO;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * SRV-C17 ReportController HTTP 매핑
 *
 * POST /api/reports           일일 업무 리포트 제출
 * GET  /api/reports?staffId=  스태프 리포트 목록 조회
 */
public class ReportHandler extends BaseHandler {

    private final ReportController reportController;

    public ReportHandler(ReportController reportController) {
        this.reportController = reportController;
    }

    @Override
    protected void dispatch(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path   = ex.getRequestURI().getPath();

        if ("POST".equals(method) && "/api/reports".equals(path)) {
            handleSubmit(ex);
        } else if ("GET".equals(method) && "/api/reports".equals(path)) {
            handleList(ex);
        } else {
            sendError(ex, 404, "Not Found: " + method + " " + path);
        }
    }

    private void handleSubmit(HttpExchange ex) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));
        DailyWorkReportDTO dto = new DailyWorkReportDTO();
        dto.setStaffId(body.get("staffId"));
        if (body.get("reportDate") != null) dto.setReportDate(LocalDate.parse(body.get("reportDate")));
        dto.setSpecialNotes(body.get("specialNotes"));
        dto.setAdditionalCostNote(body.get("additionalCostNote"));
        if (body.get("additionalCostUSD") != null) dto.setAdditionalCostUSD(new BigDecimal(body.get("additionalCostUSD")));
        sendJson(ex, 201, reportToMap(reportController.submitReport(dto)));
    }

    private void handleList(HttpExchange ex) throws IOException {
        String staffId = queryParam(ex, "staffId");
        if (staffId == null) { sendError(ex, 400, "Query parameter 'staffId' is required."); return; }
        List<DailyWorkReportDTO> list = reportController.getReportsByStaff(staffId);
        List<Map<String, Object>> items = new ArrayList<>();
        for (DailyWorkReportDTO r : list) items.add(reportToMap(r));
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("count", items.size()); resp.put("items", items);
        sendJson(ex, 200, resp);
    }

    private Map<String, Object> reportToMap(DailyWorkReportDTO dto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("staffId",             dto.getStaffId());
        m.put("reportDate",          dto.getReportDate() != null ? dto.getReportDate().toString() : null);
        m.put("specialNotes",        dto.getSpecialNotes());
        m.put("additionalCostUSD",   dto.getAdditionalCostUSD());
        m.put("additionalCostNote",  dto.getAdditionalCostNote());
        return m;
    }
}
