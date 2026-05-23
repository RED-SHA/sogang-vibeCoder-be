package com.kmedical.http;

import com.kmedical.control.InvoiceController;
import com.kmedical.dto.invoice.InvoiceCreateRequestDTO;
import com.kmedical.dto.invoice.InvoiceDTO;
import com.kmedical.dto.invoice.InvoiceItemDTO;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * SRV-C15 InvoiceController HTTP 매핑
 *
 * POST /api/invoices               인보이스 DRAFT 생성
 * POST /api/invoices/{id}/issue    ISSUED 상태로 발행
 * POST /api/invoices/{id}/cancel   CANCELLED 처리
 * GET  /api/invoices/{id}          인보이스 단건 조회
 * GET  /api/invoices?journeyId=    여정별 인보이스 목록
 */
public class InvoiceHandler extends BaseHandler {

    private final InvoiceController invoiceController;

    public InvoiceHandler(InvoiceController invoiceController) {
        this.invoiceController = invoiceController;
    }

    @Override
    protected void dispatch(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path   = ex.getRequestURI().getPath();
        String[] parts = path.split("/");

        if ("POST".equals(method) && "/api/invoices".equals(path)) {
            handleCreate(ex);
        } else if ("GET".equals(method) && "/api/invoices".equals(path)) {
            handleListByJourney(ex);
        } else if ("GET".equals(method) && parts.length == 4) {
            handleGet(ex, parts[3]);
        } else if ("POST".equals(method) && parts.length == 5 && "issue".equals(parts[4])) {
            handleIssue(ex, parts[3]);
        } else if ("POST".equals(method) && parts.length == 5 && "cancel".equals(parts[4])) {
            handleCancel(ex, parts[3]);
        } else {
            sendError(ex, 404, "Not Found: " + method + " " + path);
        }
    }

    private void handleCreate(HttpExchange ex) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));
        InvoiceCreateRequestDTO req = new InvoiceCreateRequestDTO();
        req.setPatientJourneyId(body.get("patientJourneyId"));
        req.setPatientId(body.get("patientId"));
        req.setIssuedBy(body.get("issuedBy"));
        if (body.get("serviceDescription") != null) {
            InvoiceItemDTO item = new InvoiceItemDTO();
            item.setServiceDescription(body.get("serviceDescription"));
            if (body.get("amountUSD") != null) item.setAmountUSD(new BigDecimal(body.get("amountUSD")));
            req.setItems(Collections.singletonList(item));
        }
        sendJson(ex, 201, invoiceToMap(invoiceController.createInvoice(req)));
    }

    private void handleIssue(HttpExchange ex, String id) throws IOException {
        sendJson(ex, 200, invoiceToMap(invoiceController.issueInvoice(id)));
    }

    private void handleCancel(HttpExchange ex, String id) throws IOException {
        sendJson(ex, 200, invoiceToMap(invoiceController.cancelInvoice(id)));
    }

    private void handleGet(HttpExchange ex, String id) throws IOException {
        sendJson(ex, 200, invoiceToMap(invoiceController.getInvoice(id)));
    }

    private void handleListByJourney(HttpExchange ex) throws IOException {
        String journeyId = queryParam(ex, "journeyId");
        if (journeyId == null) { sendError(ex, 400, "Query parameter 'journeyId' is required."); return; }
        List<InvoiceDTO> list = invoiceController.getInvoicesByJourney(journeyId);
        List<Map<String, Object>> items = new ArrayList<>();
        for (InvoiceDTO i : list) items.add(invoiceToMap(i));
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("count", items.size()); resp.put("items", items);
        sendJson(ex, 200, resp);
    }

    private Map<String, Object> invoiceToMap(InvoiceDTO dto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("invoiceId",        dto.getInvoiceId());
        m.put("patientJourneyId", dto.getPatientJourneyId());
        m.put("patientId",        dto.getPatientId());
        m.put("issuedBy",         dto.getIssuedBy());
        m.put("totalAmountUSD",   dto.getTotalAmountUSD());
        m.put("status",           dto.getStatus() != null ? dto.getStatus().name() : null);
        m.put("pdfUrl",           dto.getPdfUrl());
        m.put("issuedAt",         dto.getIssuedAt() != null ? dto.getIssuedAt().toString() : null);
        m.put("sentAt",           dto.getSentAt() != null ? dto.getSentAt().toString() : null);
        return m;
    }
}
