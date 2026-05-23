package com.kmedical.http;

import com.kmedical.control.QuotationController;
import com.kmedical.domain.enums.RequiredServiceType;
import com.kmedical.dto.quotation.*;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * /api/quotations/** 요청 처리
 *
 * POST /api/quotations/requests           UC-P07 환자 견적 요청
 * GET  /api/quotations/requests/{id}      견적 요청 조회
 * POST /api/quotations/issue              UC-A04 관리자 견적 발송
 * POST /api/quotations/accept             UC-P08 환자 견적 수락
 * GET  /api/quotations?requestId={id}     견적 목록 조회
 */
public class QuotationHandler extends BaseHandler {

    private final QuotationController quotationController;

    public QuotationHandler(QuotationController quotationController) {
        this.quotationController = quotationController;
    }

    @Override
    protected void dispatch(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path   = ex.getRequestURI().getPath();

        if ("POST".equals(method) && "/api/quotations/requests".equals(path)) {
            handleCreateRequest(ex);
        } else if ("GET".equals(method) && path.startsWith("/api/quotations/requests/")) {
            String id = path.substring("/api/quotations/requests/".length());
            handleGetRequest(ex, id);
        } else if ("POST".equals(method) && "/api/quotations/issue".equals(path)) {
            handleIssueQuotation(ex);
        } else if ("POST".equals(method) && "/api/quotations/accept".equals(path)) {
            handleAcceptQuotation(ex);
        } else if ("GET".equals(method) && "/api/quotations".equals(path)) {
            handleListQuotations(ex);
        } else {
            sendError(ex, 404, "Not Found: " + method + " " + path);
        }
    }

    // ── UC-P07: 환자 견적 요청 생성 ───────────────────────────────────────────
    private void handleCreateRequest(HttpExchange ex) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));

        QuotationCreateRequestDTO req = new QuotationCreateRequestDTO();
        req.setPatientId(body.get("patientId"));
        if (body.get("desiredVisitDate") != null) {
            req.setDesiredVisitDate(LocalDate.parse(body.get("desiredVisitDate")));
        }
        req.setSurgeryType(body.get("surgeryType"));
        if (body.get("requiredService") != null) {
            req.setRequiredServices(Collections.singletonList(
                    RequiredServiceType.valueOf(body.get("requiredService"))));
        }

        QuotationRequestDTO result = quotationController.createQuotationRequest(req);
        sendJson(ex, 201, rfqToMap(result));
    }

    // ── 견적 요청 단건 조회 ────────────────────────────────────────────────────
    private void handleGetRequest(HttpExchange ex, String id) throws IOException {
        QuotationRequestDTO result = quotationController.getQuotationRequest(id);
        sendJson(ex, 200, rfqToMap(result));
    }

    // ── UC-A04: 관리자 견적 발송 ───────────────────────────────────────────────
    private void handleIssueQuotation(HttpExchange ex) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));

        QuotationCreateRequestDTO req = new QuotationCreateRequestDTO();
        req.setQuotationRequestId(body.get("quotationRequestId"));
        req.setPatientId(body.get("patientId"));
        if (body.get("medicalFeeUSD") != null) {
            req.setMedicalFeeUSD(new BigDecimal(body.get("medicalFeeUSD")));
        }
        if (body.get("conciergeFeeUSD") != null) {
            req.setConciergeFeeUSD(new BigDecimal(body.get("conciergeFeeUSD")));
        }

        QuotationDTO result = quotationController.issueQuotation(req);
        sendJson(ex, 201, quotationToMap(result));
    }

    // ── UC-P08: 환자 견적 수락 ─────────────────────────────────────────────────
    private void handleAcceptQuotation(HttpExchange ex) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));

        QuotationAcceptRequestDTO req = new QuotationAcceptRequestDTO();
        req.setQuotationId(body.get("quotationId"));

        QuotationDTO result = quotationController.acceptQuotation(req);
        sendJson(ex, 200, quotationToMap(result));
    }

    // ── 견적 목록 조회 ─────────────────────────────────────────────────────────
    private void handleListQuotations(HttpExchange ex) throws IOException {
        String requestId = queryParam(ex, "requestId");
        if (requestId == null || requestId.isBlank()) {
            sendError(ex, 400, "Query parameter 'requestId' is required.");
            return;
        }
        List<QuotationDTO> list = quotationController.getQuotationsForRequest(requestId);
        List<Map<String, Object>> items = new ArrayList<>();
        for (QuotationDTO q : list) items.add(quotationToMap(q));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("count", items.size());
        response.put("items", items);
        sendJson(ex, 200, response);
    }

    // ── DTO → Map 변환 ─────────────────────────────────────────────────────────

    private Map<String, Object> rfqToMap(QuotationRequestDTO dto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("quotationRequestId", dto.getQuotationRequestId());
        m.put("patientId",          dto.getPatientId());
        m.put("surgeryType",        dto.getSurgeryType());
        m.put("status",             dto.getStatus() != null ? dto.getStatus().name() : null);
        m.put("expiresAt",          dto.getExpiresAt() != null ? dto.getExpiresAt().toString() : null);
        m.put("createdAt",          dto.getCreatedAt() != null ? dto.getCreatedAt().toString() : null);
        return m;
    }

    private Map<String, Object> quotationToMap(QuotationDTO dto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("quotationId",        dto.getQuotationId());
        m.put("quotationRequestId", dto.getQuotationRequestId());
        m.put("medicalFeeUSD",      dto.getMedicalFeeUSD() != null ? dto.getMedicalFeeUSD().toPlainString() : null);
        m.put("conciergeFeeUSD",    dto.getConciergeFeeUSD() != null ? dto.getConciergeFeeUSD().toPlainString() : null);
        m.put("totalFeeUSD",        dto.getTotalFeeUSD() != null ? dto.getTotalFeeUSD().toPlainString() : null);
        m.put("status",             dto.getStatus() != null ? dto.getStatus().name() : null);
        m.put("sentAt",             dto.getSentAt() != null ? dto.getSentAt().toString() : null);
        m.put("acceptedAt",         dto.getAcceptedAt() != null ? dto.getAcceptedAt().toString() : null);
        return m;
    }
}
