package com.kmedical.control;

import com.kmedical.domain.entity.Invoice;
import com.kmedical.domain.entity.InvoiceItem;
import com.kmedical.domain.enums.AlertChannel;
import com.kmedical.domain.enums.AlertType;
import com.kmedical.domain.enums.InvoiceStatus;
import com.kmedical.dto.alert.AlertCreateRequestDTO;
import com.kmedical.dto.invoice.InvoiceCreateRequestDTO;
import com.kmedical.dto.invoice.InvoiceDTO;
import com.kmedical.dto.invoice.InvoiceItemDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SRV-C15 — InvoiceController
 * 책임: 인보이스 발행, PDF 생성, 열람 링크 발급 (ISSUED 이후 수정 금지).
 * UC: UC-A11, UC-P14
 * 제약: ISSUED 상태 수정 불가; CANCELLED 후 재발행; Invoice ISSUED 시 WorkProofPhoto 보관 갱신
 */
public class InvoiceController {

    private final AccessLinkController accessLinkController;
    private final WorkController workController;
    private final AlertController alertController;
    private final Map<String, Invoice> invoiceStore = new HashMap<>();
    private final Map<String, List<InvoiceItem>> itemStore = new HashMap<>();

    public InvoiceController(AccessLinkController accessLinkController,
                              WorkController workController,
                              AlertController alertController) {
        this.accessLinkController = accessLinkController;
        this.workController = workController;
        this.alertController = alertController;
    }

    private void guardNotClosedDown() {
        if (SystemStateRegistry.getInstance().isClosedDown()) {
            throw new IllegalStateException("System is closed down. Customer operations are not permitted.");
        }
    }

    /**
     * 인보이스를 DRAFT 상태로 생성한다.
     * System Response: 입력 검증 → Invoice 및 InvoiceItem 저장 → 총액 계산
     */
    public InvoiceDTO createInvoice(InvoiceCreateRequestDTO request) {
        guardNotClosedDown();
        if (request == null || request.getPatientJourneyId() == null) {
            throw new IllegalArgumentException("Invoice creation request is incomplete.");
        }

        Invoice invoice = new Invoice();
        invoice.setInvoiceId(UUID.randomUUID().toString());
        invoice.setPatientJourneyId(request.getPatientJourneyId());
        invoice.setPatientId(request.getPatientId());
        invoice.setIssuedBy(request.getIssuedBy());
        invoice.setStatus(InvoiceStatus.DRAFT);

        BigDecimal total = BigDecimal.ZERO;
        List<InvoiceItem> items = new ArrayList<>();
        if (request.getItems() != null) {
            for (InvoiceItemDTO itemDto : request.getItems()) {
                InvoiceItem item = new InvoiceItem();
                item.setInvoiceItemId(UUID.randomUUID().toString());
                item.setInvoiceId(invoice.getInvoiceId());
                item.setServiceDescription(itemDto.getServiceDescription());
                item.setAmountUSD(itemDto.getAmountUSD());
                item.setSortOrder(itemDto.getSortOrder());
                items.add(item);
                if (itemDto.getAmountUSD() != null) total = total.add(itemDto.getAmountUSD());
            }
        }
        invoice.setTotalAmountUSD(total);
        invoiceStore.put(invoice.getInvoiceId(), invoice);
        itemStore.put(invoice.getInvoiceId(), items);

        return toDTO(invoice);
    }

    /**
     * 인보이스를 ISSUED 상태로 발행한다.
     * System Response: DRAFT 확인 → ISSUED 전이 → WorkProofPhoto 보관 갱신 → 알림 발송
     */
    public InvoiceDTO issueInvoice(String invoiceId) {
        guardNotClosedDown();
        Invoice invoice = findInvoice(invoiceId);
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT invoices can be issued.");
        }

        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setIssuedAt(LocalDateTime.now());

        // 제약#8: Invoice ISSUED 일시 기준 1년으로 해당 여정의 WorkProofPhoto 보관 기한 갱신
        workController.extendRetentionOnInvoiceIssued(invoice.getPatientJourneyId(), invoice.getIssuedAt());

        alertController.sendAlert(new AlertCreateRequestDTO(
                AlertType.INVOICE_ISSUED, AlertChannel.PUSH, invoice.getPatientId(), "Your invoice has been issued."));

        return toDTO(invoice);
    }

    /**
     * 인보이스를 CANCELLED 상태로 취소한다.
     * System Response: ISSUED 확인 → CANCELLED 전이 (이후 재발행 가능)
     */
    public InvoiceDTO cancelInvoice(String invoiceId) {
        guardNotClosedDown();
        Invoice invoice = findInvoice(invoiceId);
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException("Invoice is already cancelled.");
        }
        if (invoice.getStatus() == InvoiceStatus.DRAFT) {
            throw new IllegalStateException("DRAFT invoices should not be cancelled via this method. Delete instead.");
        }
        invoice.setStatus(InvoiceStatus.CANCELLED);
        return toDTO(invoice);
    }

    /**
     * 인보이스를 조회한다.
     */
    public InvoiceDTO getInvoice(String invoiceId) {
        guardNotClosedDown();
        return toDTO(findInvoice(invoiceId));
    }

    /**
     * 여정의 인보이스 목록을 조회한다.
     */
    public List<InvoiceDTO> getInvoicesByJourney(String patientJourneyId) {
        guardNotClosedDown();
        List<InvoiceDTO> result = new ArrayList<>();
        for (Invoice inv : invoiceStore.values()) {
            if (inv.getPatientJourneyId().equals(patientJourneyId)) result.add(toDTO(inv));
        }
        return result;
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private Invoice findInvoice(String id) {
        Invoice inv = invoiceStore.get(id);
        if (inv == null) throw new IllegalArgumentException("Invoice not found: " + id);
        return inv;
    }

    private InvoiceDTO toDTO(Invoice inv) {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setInvoiceId(inv.getInvoiceId());
        dto.setPatientJourneyId(inv.getPatientJourneyId());
        dto.setPatientId(inv.getPatientId());
        dto.setIssuedBy(inv.getIssuedBy());
        dto.setTotalAmountUSD(inv.getTotalAmountUSD());
        dto.setStatus(inv.getStatus());
        dto.setPdfUrl(inv.getPdfUrl());
        dto.setIssuedAt(inv.getIssuedAt());
        dto.setSentAt(inv.getSentAt());
        return dto;
    }
}
