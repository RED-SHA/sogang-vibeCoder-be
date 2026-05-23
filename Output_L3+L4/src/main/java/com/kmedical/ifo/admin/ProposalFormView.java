package com.kmedical.ifo.admin;

import com.kmedical.control.AccessLinkController;
import com.kmedical.control.QuotationController;
import com.kmedical.domain.enums.AccessLinkType;
import com.kmedical.dto.accesslink.AccessLinkCreateRequestDTO;
import com.kmedical.dto.quotation.QuotationCreateRequestDTO;
import com.kmedical.dto.quotation.QuotationDTO;
import com.kmedical.dto.quotation.QuotationRequestDTO;

import java.util.List;

/**
 * IFO-A05 — ProposalFormView
 * UC: UC-A04 (견적 제안서 발송)
 * 책임: 관리자가 견적 요청을 조회하고 견적서를 작성·발송하는 UI 진입점.
 *       발송 후 환자용 AccessLink를 생성한다 (L4: SRV-C06 + SRV-C02 연계).
 */
public class ProposalFormView {

    private final QuotationController quotationController;
    private final AccessLinkController accessLinkController;

    public ProposalFormView(QuotationController quotationController,
                             AccessLinkController accessLinkController) {
        this.quotationController = quotationController;
        this.accessLinkController = accessLinkController;
    }

    /**
     * 관리자가 견적 요청을 조회한다.
     * Actor Action: Admin views a quotation request from a patient.
     */
    public QuotationRequestDTO viewQuotationRequest(String quotationRequestId) {
        return quotationController.getQuotationRequest(quotationRequestId);
    }

    /**
     * 관리자가 견적서를 작성하고 발송하며 환자용 접속 링크를 생성한다.
     * Actor Action: Admin issues a quotation → AccessLink(PATIENT_PROPOSAL) created for patient.
     */
    public QuotationDTO issueQuotation(QuotationCreateRequestDTO request) {
        QuotationDTO quotation = quotationController.issueQuotation(request);

        AccessLinkCreateRequestDTO linkRequest = new AccessLinkCreateRequestDTO();
        linkRequest.setLinkType(AccessLinkType.PATIENT_PROPOSAL);
        linkRequest.setTargetId(quotation.getQuotationId());
        linkRequest.setRecipientUserId(request.getPatientId());
        accessLinkController.createAccessLink(linkRequest);

        return quotation;
    }

    /**
     * 관리자가 특정 견적 요청에 대한 견적 목록을 조회한다.
     * Actor Action: Admin views all quotations for a request.
     */
    public List<QuotationDTO> viewQuotationsForRequest(String quotationRequestId) {
        return quotationController.getQuotationsForRequest(quotationRequestId);
    }
}
