package com.kmedical.http;

import com.kmedical.control.PassportController;
import com.kmedical.domain.enums.PassportReviewStatus;
import com.kmedical.dto.passport.*;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SRV-C03 PassportController HTTP 매핑
 *
 * POST /api/passports/upload   UC-P03 여권 업로드 + OCR
 * POST /api/passports/review   UC-A03 관리자 검토
 * GET  /api/passports/{id}     여권 정보 조회
 */
public class PassportHandler extends BaseHandler {

    private final PassportController passportController;

    public PassportHandler(PassportController passportController) {
        this.passportController = passportController;
    }

    @Override
    protected void dispatch(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path   = ex.getRequestURI().getPath();
        String[] parts = path.split("/");

        if ("POST".equals(method) && "/api/passports/upload".equals(path)) {
            handleUpload(ex);
        } else if ("POST".equals(method) && "/api/passports/review".equals(path)) {
            handleReview(ex);
        } else if ("GET".equals(method) && parts.length == 4) {
            handleGet(ex, parts[3]);
        } else {
            sendError(ex, 404, "Not Found: " + method + " " + path);
        }
    }

    private void handleUpload(HttpExchange ex) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));

        PassportUploadRequestDTO req = new PassportUploadRequestDTO();
        req.setPatientId(body.get("patientId"));
        req.setImageUrl(body.get("imageUrl"));

        PassportInfoDTO result = passportController.uploadAndOcr(req);
        sendJson(ex, 201, passportToMap(result));
    }

    private void handleReview(HttpExchange ex) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));

        PassportReviewRequestDTO req = new PassportReviewRequestDTO();
        req.setPassportInfoId(body.get("passportInfoId"));
        req.setReviewedBy(body.get("reviewedBy"));
        if (body.get("reviewStatus") != null) req.setReviewStatus(PassportReviewStatus.valueOf(body.get("reviewStatus")));
        req.setConfirmedFullNameEn(body.get("confirmedFullNameEn"));
        req.setConfirmedPassportNumber(body.get("confirmedPassportNumber"));
        req.setRejectionReason(body.get("rejectionReason"));

        PassportInfoDTO result = passportController.reviewPassport(req);
        sendJson(ex, 200, passportToMap(result));
    }

    private void handleGet(HttpExchange ex, String id) throws IOException {
        PassportInfoDTO result = passportController.getPassportInfo(id);
        sendJson(ex, 200, passportToMap(result));
    }

    private Map<String, Object> passportToMap(PassportInfoDTO dto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("passportInfoId",          dto.getPassportInfoId());
        m.put("patientId",               dto.getPatientId());
        m.put("imageUrl",                dto.getImageUrl());
        m.put("ocrFullNameEn",           dto.getOcrFullNameEn());
        m.put("ocrPassportNumber",       dto.getOcrPassportNumber());
        m.put("ocrNationality",          dto.getOcrNationality());
        m.put("ocrExpiryDate",           dto.getOcrExpiryDate() != null ? dto.getOcrExpiryDate().toString() : null);
        m.put("confirmedFullNameEn",     dto.getConfirmedFullNameEn());
        m.put("confirmedPassportNumber", dto.getConfirmedPassportNumber());
        m.put("reviewStatus",            dto.getReviewStatus() != null ? dto.getReviewStatus().name() : null);
        m.put("reviewedBy",              dto.getReviewedBy());
        m.put("reviewedAt",              dto.getReviewedAt() != null ? dto.getReviewedAt().toString() : null);
        m.put("rejectionReason",         dto.getRejectionReason());
        return m;
    }
}
