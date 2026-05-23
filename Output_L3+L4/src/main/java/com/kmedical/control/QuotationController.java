package com.kmedical.control;

import com.kmedical.domain.entity.Quotation;
import com.kmedical.domain.entity.QuotationRequest;
import com.kmedical.domain.enums.QuotationRequestStatus;
import com.kmedical.domain.enums.QuotationStatus;
import com.kmedical.dto.quotation.QuotationAcceptRequestDTO;
import com.kmedical.dto.quotation.QuotationCreateRequestDTO;
import com.kmedical.dto.quotation.QuotationDTO;
import com.kmedical.dto.quotation.QuotationRequestDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SRV-C06 — QuotationController
 * 책임: RFQ 처리, 견적 발행/수락, 상태 전이 (CLOSED·EXPIRED·CANCELLED).
 * UC: UC-A04, UC-P07, UC-P08
 * 제약:
 *   - OPEN 상태 요청: 환자당 최대 3건
 *   - 하나 ACCEPTED 전이 시 동일 Patient의 나머지 OPEN RFQ → CLOSED
 *   - 동일 QuotationRequest에 ACCEPTED Quotation 1건만 허용
 */
public class QuotationController {

    private static final int MAX_OPEN_REQUESTS = 3;

    private final Map<String, QuotationRequest> requestStore = new HashMap<>();
    private final Map<String, Quotation> quotationStore = new HashMap<>();

    private void guardNotClosedDown() {
        if (SystemStateRegistry.getInstance().isClosedDown()) {
            throw new IllegalStateException("System is closed down. Customer operations are not permitted.");
        }
    }

    /**
     * 환자가 견적을 요청한다.
     * System Response: OPEN 건수 확인(최대 3) → QuotationRequest 생성 (expiresAt = now+7일)
     */
    public QuotationRequestDTO createQuotationRequest(QuotationCreateRequestDTO request) {
        guardNotClosedDown();
        if (request == null || request.getPatientId() == null) {
            throw new IllegalArgumentException("QuotationRequest data is incomplete.");
        }

        long openCount = requestStore.values().stream()
                .filter(r -> r.getPatientId().equals(request.getPatientId())
                        && r.getStatus() == QuotationRequestStatus.OPEN)
                .count();
        if (openCount >= MAX_OPEN_REQUESTS) {
            throw new IllegalStateException("Patient already has " + MAX_OPEN_REQUESTS + " open quotation requests.");
        }

        QuotationRequest rfq = new QuotationRequest();
        rfq.setQuotationRequestId(UUID.randomUUID().toString());
        rfq.setPatientId(request.getPatientId());
        rfq.setDesiredVisitDate(request.getDesiredVisitDate());
        rfq.setSurgeryType(request.getSurgeryType());
        rfq.setRequiredServices(request.getRequiredServices());
        rfq.setStatus(QuotationRequestStatus.OPEN);
        rfq.setCreatedAt(LocalDateTime.now());
        rfq.setExpiresAt(LocalDateTime.now().plusDays(7));

        requestStore.put(rfq.getQuotationRequestId(), rfq);
        return toRFQDTO(rfq);
    }

    /**
     * 견적서를 조회한다.
     */
    public QuotationRequestDTO getQuotationRequest(String quotationRequestId) {
        guardNotClosedDown();
        QuotationRequest rfq = findRFQ(quotationRequestId);
        return toRFQDTO(rfq);
    }

    /**
     * 관리자가 견적서를 발행한다.
     * System Response: RFQ 상태 확인 → Quotation 생성(DRAFT) → 발송(SENT)
     */
    public QuotationDTO issueQuotation(QuotationCreateRequestDTO request) {
        guardNotClosedDown();
        if (request == null || request.getQuotationRequestId() == null) {
            throw new IllegalArgumentException("Quotation issue request is incomplete.");
        }

        QuotationRequest rfq = findRFQ(request.getQuotationRequestId());
        if (rfq.getStatus() != QuotationRequestStatus.OPEN) {
            throw new IllegalStateException("QuotationRequest is not in OPEN state.");
        }

        Quotation q = new Quotation();
        q.setQuotationId(UUID.randomUUID().toString());
        q.setQuotationRequestId(rfq.getQuotationRequestId());
        q.setMedicalFeeUSD(request.getMedicalFeeUSD());
        q.setConciergeFeeUSD(request.getConciergeFeeUSD());
        BigDecimal total = (request.getMedicalFeeUSD() != null ? request.getMedicalFeeUSD() : BigDecimal.ZERO)
                .add(request.getConciergeFeeUSD() != null ? request.getConciergeFeeUSD() : BigDecimal.ZERO);
        q.setTotalFeeUSD(total);
        q.setStatus(QuotationStatus.SENT);
        q.setSentAt(LocalDateTime.now());

        quotationStore.put(q.getQuotationId(), q);
        return toQuotationDTO(q);
    }

    /**
     * 환자가 견적서를 수락한다.
     * System Response: 중복 ACCEPTED 확인 → Quotation ACCEPTED → 나머지 OPEN RFQ CLOSED
     */
    public QuotationDTO acceptQuotation(QuotationAcceptRequestDTO request) {
        guardNotClosedDown();
        if (request == null || request.getQuotationId() == null) {
            throw new IllegalArgumentException("Accept request is incomplete.");
        }

        Quotation quotation = quotationStore.get(request.getQuotationId());
        if (quotation == null) throw new IllegalArgumentException("Quotation not found: " + request.getQuotationId());
        if (quotation.getStatus() != QuotationStatus.SENT) {
            throw new IllegalStateException("Quotation is not in SENT state.");
        }

        boolean alreadyAccepted = quotationStore.values().stream()
                .anyMatch(q -> q.getQuotationRequestId().equals(quotation.getQuotationRequestId())
                        && q.getStatus() == QuotationStatus.ACCEPTED);
        if (alreadyAccepted) {
            throw new IllegalStateException("A quotation for this request is already accepted.");
        }

        quotation.setStatus(QuotationStatus.ACCEPTED);
        quotation.setAcceptedAt(LocalDateTime.now());

        QuotationRequest rfq = findRFQ(quotation.getQuotationRequestId());
        rfq.setStatus(QuotationRequestStatus.ACCEPTED);

        closeOtherOpenRequests(rfq.getPatientId(), rfq.getQuotationRequestId());

        return toQuotationDTO(quotation);
    }

    /**
     * 만료된 QuotationRequest를 EXPIRED로 전이한다 (스케줄러 호출용).
     */
    public void expireOverdueRequests() {
        LocalDateTime now = LocalDateTime.now();
        for (QuotationRequest rfq : requestStore.values()) {
            if (rfq.getStatus() == QuotationRequestStatus.OPEN && rfq.getExpiresAt().isBefore(now)) {
                rfq.setStatus(QuotationRequestStatus.EXPIRED);
                expireQuotationsFor(rfq.getQuotationRequestId());
            }
        }
    }

    /**
     * 환자의 견적 요청 목록을 조회한다.
     */
    public List<QuotationRequestDTO> getRequestsByPatient(String patientId) {
        guardNotClosedDown();
        List<QuotationRequestDTO> result = new ArrayList<>();
        for (QuotationRequest rfq : requestStore.values()) {
            if (rfq.getPatientId().equals(patientId)) result.add(toRFQDTO(rfq));
        }
        return result;
    }

    /**
     * 에이전시의 OPEN 상태 견적 요청 목록을 조회한다 (대시보드용).
     */
    public List<QuotationRequestDTO> getOpenRequests(String agencyId) {
        guardNotClosedDown();
        List<QuotationRequestDTO> result = new ArrayList<>();
        for (QuotationRequest rfq : requestStore.values()) {
            if (rfq.getStatus() == QuotationRequestStatus.OPEN) result.add(toRFQDTO(rfq));
        }
        return result;
    }

    /**
     * 견적서 목록을 조회한다.
     */
    public List<QuotationDTO> getQuotationsForRequest(String quotationRequestId) {
        guardNotClosedDown();
        List<QuotationDTO> result = new ArrayList<>();
        for (Quotation q : quotationStore.values()) {
            if (q.getQuotationRequestId().equals(quotationRequestId)) {
                result.add(toQuotationDTO(q));
            }
        }
        return result;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void closeOtherOpenRequests(String patientId, String acceptedRfqId) {
        for (QuotationRequest rfq : requestStore.values()) {
            if (rfq.getPatientId().equals(patientId)
                    && rfq.getStatus() == QuotationRequestStatus.OPEN
                    && !rfq.getQuotationRequestId().equals(acceptedRfqId)) {
                rfq.setStatus(QuotationRequestStatus.CLOSED);
                expireQuotationsFor(rfq.getQuotationRequestId());
            }
        }
    }

    private void expireQuotationsFor(String quotationRequestId) {
        for (Quotation q : quotationStore.values()) {
            if (q.getQuotationRequestId().equals(quotationRequestId)
                    && (q.getStatus() == QuotationStatus.SENT || q.getStatus() == QuotationStatus.DRAFT)) {
                q.setStatus(QuotationStatus.EXPIRED);
            }
        }
    }

    private QuotationRequest findRFQ(String id) {
        QuotationRequest rfq = requestStore.get(id);
        if (rfq == null) throw new IllegalArgumentException("QuotationRequest not found: " + id);
        return rfq;
    }

    private QuotationRequestDTO toRFQDTO(QuotationRequest rfq) {
        QuotationRequestDTO dto = new QuotationRequestDTO();
        dto.setQuotationRequestId(rfq.getQuotationRequestId());
        dto.setPatientId(rfq.getPatientId());
        dto.setDesiredVisitDate(rfq.getDesiredVisitDate());
        dto.setSurgeryType(rfq.getSurgeryType());
        dto.setRequiredServices(rfq.getRequiredServices());
        dto.setStatus(rfq.getStatus());
        dto.setExpiresAt(rfq.getExpiresAt());
        dto.setCreatedAt(rfq.getCreatedAt());
        return dto;
    }

    private QuotationDTO toQuotationDTO(Quotation q) {
        QuotationDTO dto = new QuotationDTO();
        dto.setQuotationId(q.getQuotationId());
        dto.setQuotationRequestId(q.getQuotationRequestId());
        dto.setAgencyId(q.getAgencyId());
        dto.setMedicalFeeUSD(q.getMedicalFeeUSD());
        dto.setConciergeFeeUSD(q.getConciergeFeeUSD());
        dto.setTotalFeeUSD(q.getTotalFeeUSD());
        dto.setStatus(q.getStatus());
        dto.setSentAt(q.getSentAt());
        dto.setAcceptedAt(q.getAcceptedAt());
        return dto;
    }
}
