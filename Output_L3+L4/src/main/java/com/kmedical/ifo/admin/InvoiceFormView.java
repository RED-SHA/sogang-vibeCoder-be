package com.kmedical.ifo.admin;

import com.kmedical.control.AccessLinkController;
import com.kmedical.control.InvoiceController;
import com.kmedical.domain.enums.AccessLinkType;
import com.kmedical.dto.accesslink.AccessLinkCreateRequestDTO;
import com.kmedical.dto.invoice.InvoiceCreateRequestDTO;
import com.kmedical.dto.invoice.InvoiceDTO;

import java.util.List;

/**
 * IFO-A12 — InvoiceFormView
 * UC: UC-A11 (인보이스 발행)
 * 책임: 관리자가 인보이스를 작성·발행·취소하는 UI 진입점.
 *       발행 후 환자용 AccessLink(INVOICE_VIEW)를 생성한다 (L4: SRV-C15 + SRV-C02 연계).
 */
public class InvoiceFormView {

    private final InvoiceController invoiceController;
    private final AccessLinkController accessLinkController;

    public InvoiceFormView(InvoiceController invoiceController,
                            AccessLinkController accessLinkController) {
        this.invoiceController = invoiceController;
        this.accessLinkController = accessLinkController;
    }

    /**
     * 관리자가 인보이스를 DRAFT 상태로 생성한다.
     * Actor Action: Admin creates a draft invoice.
     */
    public InvoiceDTO createDraftInvoice(InvoiceCreateRequestDTO request) {
        return invoiceController.createInvoice(request);
    }

    /**
     * 관리자가 인보이스를 발행(ISSUED)하고 환자용 접속 링크를 생성한다.
     * Actor Action: Admin issues the invoice → AccessLink(INVOICE_VIEW) created for patient.
     */
    public InvoiceDTO issueInvoice(String invoiceId) {
        InvoiceDTO invoice = invoiceController.issueInvoice(invoiceId);

        AccessLinkCreateRequestDTO linkRequest = new AccessLinkCreateRequestDTO();
        linkRequest.setLinkType(AccessLinkType.INVOICE_VIEW);
        linkRequest.setTargetId(invoice.getInvoiceId());
        linkRequest.setRecipientUserId(invoice.getPatientId());
        accessLinkController.createAccessLink(linkRequest);

        return invoice;
    }

    /**
     * 관리자가 인보이스를 취소한다.
     * Actor Action: Admin cancels an issued invoice.
     */
    public InvoiceDTO cancelInvoice(String invoiceId) {
        return invoiceController.cancelInvoice(invoiceId);
    }

    /**
     * 관리자가 여정별 인보이스 목록을 조회한다.
     * Actor Action: Admin views all invoices for a patient journey.
     */
    public List<InvoiceDTO> viewInvoicesByJourney(String patientJourneyId) {
        return invoiceController.getInvoicesByJourney(patientJourneyId);
    }
}
