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
import com.kmedical.util.AuditLogger;
import com.kmedical.util.ValidationUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SRV-C15 — InvoiceController
 * 책임: 인보이스 발행, PDF 생성, 열람 링크 발급 (ISSUED 이후 수정 금지).
 * UC: UC-A11, UC-P14
 * NFR 적용: ConcurrentHashMap, 금액 검증, AuditLogger(INVOICE_ISSUED/CANCELLED)
 */
public class InvoiceController {

    private final AccessLinkController accessLinkController;
    private final WorkController       workController;
    private final AlertController      alertController;
    private final Map<String, Invoice>            invoiceStore = new ConcurrentHashMap<>();
    private final Map<String, List<InvoiceItem>>  itemStore    = new ConcurrentHashMap<>();

    public InvoiceController(AccessLinkController accessLinkController,
                              WorkController workController,
                              AlertController alertController) {
        this.accessLinkController = accessLinkController;
        this.workController       = workController;
        this.alertController      = alertController;
    }

    private void guardNotClosedDown() {
        if (SystemStateRegistry.getInstance().isClosedDown()) {
            AuditLogger.closedDownAccess("InvoiceController", "UNKNOWN");
            throw new IllegalStateException("System is closed down. Customer operations are not permitted.");
        }
    }

    /**
     * 인보이스를 DRAFT 상태로 생성한다.
     * 검증: patientJourneyId/patientId/issuedBy not null, 항목 금액 양수, 합산 일치
     */
    public InvoiceDTO createInvoice(InvoiceCreateRequestDTO request) {
        guardNotClosedDown();
        ValidationUtil.requireNotNull(request, "InvoiceCreateRequestDTO");
        ValidationUtil.requireNotBlank(request.getPatientJourneyId(), "patientJourneyId");
        ValidationUtil.requireNotBlank(request.getPatientId(), "patientId");
        ValidationUtil.requireNotBlank(request.getIssuedBy(), "issuedBy");

        Invoice invoice = new Invoice();
        invoice.setInvoiceId(UUID.randomUUID().toString());
        invoice.setPatientJourneyId(request.getPatientJourneyId());
        invoice.setPatientId(request.getPatientId());
        invoice.setIssuedBy(request.getIssuedBy());
        invoice.setStatus(InvoiceStatus.DRAFT);

        BigDecimal total = BigDecimal.ZERO;
        List<InvoiceItem> items = new CopyOnWriteArrayList<>();
        if (request.getItems() != null) {
            for (InvoiceItemDTO itemDto : request.getItems()) {
                ValidationUtil.requireNotBlank(itemDto.getServiceDescription(), "serviceDescription");
                ValidationUtil.requireMaxLength(itemDto.getServiceDescription(), 500, "serviceDescription");
                ValidationUtil.requirePositiveBigDecimal(itemDto.getAmountUSD(), "amountUSD");

                InvoiceItem item = new InvoiceItem();
                item.setInvoiceItemId(UUID.randomUUID().toString());
                item.setInvoiceId(invoice.getInvoiceId());
                item.setServiceDescription(itemDto.getServiceDescription());
                item.setAmountUSD(itemDto.getAmountUSD().setScale(2, java.math.RoundingMode.HALF_UP));
                item.setSortOrder(itemDto.getSortOrder() != null ? itemDto.getSortOrder() : 0);
                items.add(item);
                total = total.add(item.getAmountUSD());
            }
        }
        ValidationUtil.requireNonNegativeBigDecimal(total, "totalAmountUSD");
        invoice.setTotalAmountUSD(total.setScale(2, java.math.RoundingMode.HALF_UP));
        invoiceStore.put(invoice.getInvoiceId(), invoice);
        itemStore.put(invoice.getInvoiceId(), items);

        return toDTO(invoice);
    }

    /**
     * 인보이스를 ISSUED 상태로 발행한다.
     * 제약: DRAFT만 발행 가능. 발행 후 WorkProofPhoto 보관 갱신 + 알림 발송.
     * NFR-LOG: INVOICE_ISSUED 감사 로그
     */
    public InvoiceDTO issueInvoice(String invoiceId) {
        guardNotClosedDown();
        ValidationUtil.requireNotBlank(invoiceId, "invoiceId");
        Invoice invoice = findInvoice(invoiceId);

        try {
            if (invoice.getStatus() != InvoiceStatus.DRAFT)
                throw new IllegalStateException("Only DRAFT invoices can be issued. Current status: " + invoice.getStatus());

            invoice.setStatus(InvoiceStatus.ISSUED);
            invoice.setIssuedAt(LocalDateTime.now());

            workController.extendRetentionOnInvoiceIssued(invoice.getPatientJourneyId(), invoice.getIssuedAt());

            alertController.sendAlert(new AlertCreateRequestDTO(
                    AlertType.INVOICE_ISSUED, AlertChannel.PUSH,
                    invoice.getPatientId(), "Your invoice has been issued."));

            AuditLogger.log("INVOICE_ISSUED", invoice.getIssuedBy(), invoiceId, true,
                    "patientJourneyId=" + invoice.getPatientJourneyId());
            return toDTO(invoice);

        } catch (Exception e) {
            AuditLogger.log("INVOICE_ISSUED", invoice.getIssuedBy(), invoiceId, false, e.getMessage());
            if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) throw e;
            throw new IllegalStateException("Invoice issue failed: " + e.getMessage());
        }
    }

    /**
     * 인보이스를 CANCELLED 상태로 취소한다.
     * 제약: ISSUED 상태만 취소 가능 (DRAFT → CANCELLED 금지, 이미 CANCELLED 금지).
     * NFR-LOG: INVOICE_CANCELLED 감사 로그
     */
    public InvoiceDTO cancelInvoice(String invoiceId) {
        guardNotClosedDown();
        ValidationUtil.requireNotBlank(invoiceId, "invoiceId");
        Invoice invoice = findInvoice(invoiceId);

        try {
            if (invoice.getStatus() == InvoiceStatus.CANCELLED)
                throw new IllegalStateException("Invoice is already cancelled.");
            if (invoice.getStatus() == InvoiceStatus.DRAFT)
                throw new IllegalStateException("DRAFT invoices should not be cancelled via this method. Delete instead.");

            invoice.setStatus(InvoiceStatus.CANCELLED);

            AuditLogger.log("INVOICE_CANCELLED", invoice.getIssuedBy(), invoiceId, true,
                    "previousStatus=ISSUED patientId=" + invoice.getPatientId());
            return toDTO(invoice);

        } catch (Exception e) {
            AuditLogger.log("INVOICE_CANCELLED", invoice.getIssuedBy(), invoiceId, false, e.getMessage());
            if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) throw e;
            throw new IllegalStateException("Invoice cancel failed: " + e.getMessage());
        }
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
        ValidationUtil.requireNotBlank(patientJourneyId, "patientJourneyId");
        List<InvoiceDTO> result = new ArrayList<>();
        for (Invoice inv : invoiceStore.values()) {
            if (patientJourneyId.equals(inv.getPatientJourneyId())) result.add(toDTO(inv));
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
