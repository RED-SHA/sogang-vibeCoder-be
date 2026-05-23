package com.kmedical.ifo.staff;

import com.kmedical.control.ReportController;
import com.kmedical.dto.staff.DailyWorkReportDTO;

import java.util.List;

/**
 * IFO-S10 — DailyReportForm
 * UC: UC-S11 (일일 업무 리포트 제출)
 * 책임: 스태프가 당일 업무 리포트를 작성·제출하고 이전 리포트를 조회하는 UI 진입점.
 *       자정(midnight) 이전 제출 제약은 ReportController에서 강제된다.
 */
public class DailyReportForm {

    private final ReportController reportController;

    public DailyReportForm(ReportController reportController) {
        this.reportController = reportController;
    }

    /**
     * 스태프가 일일 업무 리포트를 제출한다.
     * Actor Action: Staff submits the daily work report.
     */
    public DailyWorkReportDTO submitReport(DailyWorkReportDTO dto) {
        return reportController.submitReport(dto);
    }

    /**
     * 스태프가 자신의 리포트 목록을 조회한다.
     * Actor Action: Staff views their submitted reports.
     */
    public List<DailyWorkReportDTO> viewMyReports(String staffId) {
        return reportController.getReportsByStaff(staffId);
    }
}
