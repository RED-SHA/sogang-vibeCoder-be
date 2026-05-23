package com.kmedical.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * C29 — DailyWorkReport «entity»
 * 제약: 업무 당일 자정 이전까지만 제출 가능
 */
public class DailyWorkReport {

    private String dailyWorkReportId;
    private String staffId;
    private LocalDate reportDate;
    private String specialNotes;
    private BigDecimal additionalCostUSD;
    private String additionalCostNote;
    private LocalDateTime submittedAt;

    public DailyWorkReport() {}

    public String getDailyWorkReportId() { return dailyWorkReportId; }
    public void setDailyWorkReportId(String dailyWorkReportId) { this.dailyWorkReportId = dailyWorkReportId; }

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }

    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }

    public String getSpecialNotes() { return specialNotes; }
    public void setSpecialNotes(String specialNotes) { this.specialNotes = specialNotes; }

    public BigDecimal getAdditionalCostUSD() { return additionalCostUSD; }
    public void setAdditionalCostUSD(BigDecimal additionalCostUSD) { this.additionalCostUSD = additionalCostUSD; }

    public String getAdditionalCostNote() { return additionalCostNote; }
    public void setAdditionalCostNote(String additionalCostNote) { this.additionalCostNote = additionalCostNote; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}
