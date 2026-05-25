package com.kmedical.control;

import com.kmedical.domain.entity.DailyWorkReport;
import com.kmedical.dto.staff.DailyWorkReportDTO;
import com.kmedical.util.AuditLogger;
import com.kmedical.util.ValidationUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SRV-C17 — ReportController
 * 책임: 일일 리포트 제출/조회 (당일 자정 이전까지만 제출 가능).
 * UC: UC-S11
 * NFR 적용: ConcurrentHashMap, reportDate=today 검증, 자정 이전 제출 검증, AuditLogger(REPORT_SUBMITTED)
 */
public class ReportController {

    private final Map<String, DailyWorkReport> reportStore = new ConcurrentHashMap<>();

    private void guardNotClosedDown() {
        if (SystemStateRegistry.getInstance().isClosedDown()) {
            AuditLogger.closedDownAccess("ReportController", "UNKNOWN");
            throw new IllegalStateException("System is closed down. Customer operations are not permitted.");
        }
    }

    /**
     * 일일 업무 리포트를 제출한다.
     * System Response: 당일 자정 이전 여부 확인 → DailyWorkReport 저장
     * NFR-LOG: REPORT_SUBMITTED 감사 로그
     */
    public DailyWorkReportDTO submitReport(DailyWorkReportDTO dto) {
        guardNotClosedDown();
        ValidationUtil.requireNotNull(dto, "DailyWorkReportDTO");
        ValidationUtil.requireNotBlank(dto.getStaffId(), "staffId");
        ValidationUtil.requireNotNull(dto.getReportDate(), "reportDate");

        LocalDate today = LocalDate.now();
        if (!dto.getReportDate().equals(today)) {
            throw new IllegalStateException("Report can only be submitted for today's date.");
        }

        LocalDateTime midnight = today.atTime(LocalTime.MIDNIGHT).plusDays(1);
        if (LocalDateTime.now().isAfter(midnight)) {
            throw new IllegalStateException("Report submission deadline has passed (midnight).");
        }

        if (dto.getSpecialNotes() != null) {
            ValidationUtil.requireMaxLength(dto.getSpecialNotes(), 3000, "specialNotes");
        }
        if (dto.getAdditionalCostNote() != null) {
            ValidationUtil.requireMaxLength(dto.getAdditionalCostNote(), 500, "additionalCostNote");
        }
        if (dto.getAdditionalCostUSD() != null) {
            ValidationUtil.requireNonNegativeBigDecimal(dto.getAdditionalCostUSD(), "additionalCostUSD");
        }

        try {
            DailyWorkReport report = new DailyWorkReport();
            report.setDailyWorkReportId(UUID.randomUUID().toString());
            report.setStaffId(dto.getStaffId());
            report.setReportDate(dto.getReportDate());
            report.setSpecialNotes(dto.getSpecialNotes());
            report.setAdditionalCostUSD(dto.getAdditionalCostUSD());
            report.setAdditionalCostNote(dto.getAdditionalCostNote());
            report.setSubmittedAt(LocalDateTime.now());

            reportStore.put(report.getDailyWorkReportId(), report);

            AuditLogger.log("REPORT_SUBMITTED", dto.getStaffId(), report.getDailyWorkReportId(), true,
                    "reportDate=" + dto.getReportDate());
            return toDTO(report);

        } catch (Exception e) {
            AuditLogger.log("REPORT_SUBMITTED", dto.getStaffId(), "UNKNOWN", false, e.getMessage());
            if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) throw e;
            throw new IllegalStateException("Report submission failed: " + e.getMessage());
        }
    }

    /**
     * 스태프의 일일 리포트 목록을 조회한다.
     */
    public List<DailyWorkReportDTO> getReportsByStaff(String staffId) {
        guardNotClosedDown();
        ValidationUtil.requireNotBlank(staffId, "staffId");
        List<DailyWorkReportDTO> result = new ArrayList<>();
        for (DailyWorkReport r : reportStore.values()) {
            if (r.getStaffId().equals(staffId)) result.add(toDTO(r));
        }
        return result;
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private DailyWorkReportDTO toDTO(DailyWorkReport r) {
        DailyWorkReportDTO dto = new DailyWorkReportDTO();
        dto.setStaffId(r.getStaffId());
        dto.setReportDate(r.getReportDate());
        dto.setSpecialNotes(r.getSpecialNotes());
        dto.setAdditionalCostUSD(r.getAdditionalCostUSD());
        dto.setAdditionalCostNote(r.getAdditionalCostNote());
        return dto;
    }
}
