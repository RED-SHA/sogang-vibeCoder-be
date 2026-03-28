package com.k.medtour.domain.proposal.entity;

import com.k.medtour.domain.proposal.enums.ProposalStatus;
import com.k.medtour.global.common.BaseEntity;
import com.k.medtour.global.exception.BusinessException;
import com.k.medtour.global.exception.ErrorCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "proposal")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Proposal extends BaseEntity {

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProposalStatus status;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "subtotal", precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "discount_rate", precision = 5, scale = 2)
    private BigDecimal discountRate;

    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ProposalItem> items = new ArrayList<>();

    @Builder
    public Proposal(Long patientId, String title, String currency, BigDecimal discountRate,
                    LocalDateTime validUntil, String notes) {
        this.patientId = patientId;
        this.title = title;
        this.status = ProposalStatus.DRAFT;
        this.currency = currency;
        this.discountRate = discountRate != null ? discountRate : BigDecimal.ZERO;
        this.validUntil = validUntil;
        this.notes = notes;
        this.subtotal = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.totalAmount = BigDecimal.ZERO;
    }

    public void addItem(ProposalItem item) {
        this.items.add(item);
        item.assignProposal(this);
    }

    public void clearItems() {
        this.items.clear();
    }

    public void calculateAmounts() {
        this.subtotal = items.stream()
                .map(ProposalItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (this.discountRate != null && this.discountRate.compareTo(BigDecimal.ZERO) > 0) {
            this.discountAmount = this.subtotal
                    .multiply(this.discountRate)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            this.discountAmount = BigDecimal.ZERO;
        }

        this.totalAmount = this.subtotal.subtract(this.discountAmount);
    }

    public void send() {
        validateStatus(ProposalStatus.DRAFT, "DRAFT 상태의 견적서만 발송할 수 있습니다.");
        this.status = ProposalStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void accept() {
        validateStatus(ProposalStatus.SENT, "SENT 상태의 견적서만 수락할 수 있습니다.");
        this.status = ProposalStatus.ACCEPTED;
        this.respondedAt = LocalDateTime.now();
    }

    public void reject() {
        validateStatus(ProposalStatus.SENT, "SENT 상태의 견적서만 거절할 수 있습니다.");
        this.status = ProposalStatus.REJECTED;
        this.respondedAt = LocalDateTime.now();
    }

    public void updateDraft(String title, String currency, BigDecimal discountRate,
                            LocalDateTime validUntil, String notes) {
        validateStatus(ProposalStatus.DRAFT, "DRAFT 상태의 견적서만 수정할 수 있습니다.");
        if (title != null) this.title = title;
        if (currency != null) this.currency = currency;
        if (discountRate != null) this.discountRate = discountRate;
        if (validUntil != null) this.validUntil = validUntil;
        if (notes != null) this.notes = notes;
    }

    private void validateStatus(ProposalStatus expected, String message) {
        if (this.status != expected) {
            throw new BusinessException(ErrorCode.PROPOSAL_INVALID_STATUS, message);
        }
    }

    public boolean isRespondable() {
        return this.status == ProposalStatus.SENT && this.respondedAt == null;
    }
}
