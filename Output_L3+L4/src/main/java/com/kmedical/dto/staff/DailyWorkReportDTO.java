package com.kmedical.dto.staff;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Interface → ReportController 간 일일 업무 리포트 제출 DTO */
public class DailyWorkReportDTO {

    private String staffId;
    private LocalDate reportDate;
    private String specialNotes;
    private BigDecimal additionalCostUSD;
    private String additionalCostNote;

    public DailyWorkReportDTO() {}

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
}
