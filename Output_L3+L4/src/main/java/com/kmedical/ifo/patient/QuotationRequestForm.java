package com.kmedical.ifo.patient;

import com.kmedical.control.QuotationController;
import com.kmedical.dto.quotation.QuotationCreateRequestDTO;
import com.kmedical.dto.quotation.QuotationRequestDTO;

/**
 * IFO-P07 — QuotationRequestForm
 * UC: UC-P07 (견적 요청서 작성)
 * 책임: 환자가 의료 서비스 견적 요청서를 작성·제출하는 UI 진입점.
 *       OPEN 상태 최대 3건 제한은 QuotationController에서 강제된다.
 */
public class QuotationRequestForm {

    private final QuotationController quotationController;

    public QuotationRequestForm(QuotationController quotationController) {
        this.quotationController = quotationController;
    }

    /**
     * 환자가 견적 요청서를 제출한다.
     * Actor Action: Patient submits a quotation request.
     */
    public QuotationRequestDTO submitQuotationRequest(QuotationCreateRequestDTO request) {
        return quotationController.createQuotationRequest(request);
    }

    /**
     * 환자가 자신의 견적 요청 목록을 조회한다.
     * Actor Action: Patient views submitted quotation requests.
     */
    public java.util.List<QuotationRequestDTO> viewMyRequests(String patientId) {
        return quotationController.getRequestsByPatient(patientId);
    }
}
