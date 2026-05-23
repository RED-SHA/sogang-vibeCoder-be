package com.kmedical.ifo.patient;

import com.kmedical.control.JourneyController;
import com.kmedical.control.QuotationController;
import com.kmedical.dto.journey.PatientJourneyDTO;
import com.kmedical.dto.quotation.QuotationAcceptRequestDTO;
import com.kmedical.dto.quotation.QuotationDTO;

import java.util.List;

/**
 * IFO-P08 — ProposalCompareView
 * UC: UC-P08 (견적 비교 및 수락)
 * 책임: 환자가 수신된 견적서를 비교하고 수락하는 UI 진입점.
 *       수락 후 PatientJourney를 생성하는 흐름까지 조율한다 (L4: SRV-C06 + SRV-C08 연계).
 */
public class ProposalCompareView {

    private final QuotationController quotationController;
    private final JourneyController journeyController;

    public ProposalCompareView(QuotationController quotationController,
                                JourneyController journeyController) {
        this.quotationController = quotationController;
        this.journeyController = journeyController;
    }

    /**
     * 환자가 특정 견적 요청에 대한 견적 목록을 조회한다.
     * Actor Action: Patient views all received quotations for a request.
     */
    public List<QuotationDTO> viewReceivedQuotations(String quotationRequestId) {
        return quotationController.getQuotationsForRequest(quotationRequestId);
    }

    /**
     * 환자가 견적을 수락하고 여정을 생성한다.
     * Actor Action: Patient accepts a quotation → PatientJourney is created (UC-P08, L4 SRV-C06+C08).
     */
    public PatientJourneyDTO acceptQuotationAndCreateJourney(QuotationAcceptRequestDTO request,
                                                              PatientJourneyDTO journeyDTO) {
        quotationController.acceptQuotation(request);
        return journeyController.createJourney(journeyDTO);
    }
}
