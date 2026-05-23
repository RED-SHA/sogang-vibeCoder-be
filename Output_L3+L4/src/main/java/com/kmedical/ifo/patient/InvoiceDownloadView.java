package com.kmedical.ifo.patient;

import com.kmedical.control.InvoiceController;
import com.kmedical.dto.invoice.InvoiceDTO;

import java.util.List;

/**
 * IFO-P13 — InvoiceDownloadView
 * UC: UC-P14 (인보이스 열람·다운로드)
 * 책임: 환자가 발행된 인보이스를 조회하고 다운로드 링크를 확인하는 UI 진입점.
 */
public class InvoiceDownloadView {

    private final InvoiceController invoiceController;

    public InvoiceDownloadView(InvoiceController invoiceController) {
        this.invoiceController = invoiceController;
    }

    /**
     * 환자가 여정별 인보이스 목록을 조회한다.
     * Actor Action: Patient views invoices for their journey.
     */
    public List<InvoiceDTO> viewInvoicesByJourney(String patientJourneyId) {
        return invoiceController.getInvoicesByJourney(patientJourneyId);
    }

    /**
     * 환자가 특정 인보이스를 조회한다.
     * Actor Action: Patient views an invoice detail and PDF download link.
     */
    public InvoiceDTO viewInvoice(String invoiceId) {
        return invoiceController.getInvoice(invoiceId);
    }
}
