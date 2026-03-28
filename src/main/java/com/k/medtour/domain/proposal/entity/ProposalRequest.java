package com.k.medtour.domain.proposal.entity;

import com.k.medtour.domain.proposal.enums.ProposalRequestStatus;
import com.k.medtour.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "proposal_request")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProposalRequest extends BaseEntity {

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "desired_procedures", columnDefinition = "jsonb")
    private List<String> desiredProcedures;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferred_hospital_ids", columnDefinition = "jsonb")
    private List<Long> preferredHospitalIds;

    @Column(name = "arrival_date")
    private LocalDate arrivalDate;

    @Column(name = "departure_date")
    private LocalDate departureDate;

    @Column(name = "accommodation_preference", length = 50)
    private String accommodationPreference;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "concierge_services", columnDefinition = "jsonb")
    private List<String> conciergeServices;

    @Column(name = "budget_min", precision = 15, scale = 2)
    private BigDecimal budgetMin;

    @Column(name = "budget_max", precision = 15, scale = 2)
    private BigDecimal budgetMax;

    @Column(name = "budget_currency", length = 10)
    private String budgetCurrency;

    @Column(name = "additional_requests", columnDefinition = "TEXT")
    private String additionalRequests;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProposalRequestStatus status;

    @Builder
    public ProposalRequest(Long patientId, List<String> desiredProcedures, List<Long> preferredHospitalIds,
                           LocalDate arrivalDate, LocalDate departureDate, String accommodationPreference,
                           List<String> conciergeServices, BigDecimal budgetMin, BigDecimal budgetMax,
                           String budgetCurrency, String additionalRequests) {
        this.patientId = patientId;
        this.desiredProcedures = desiredProcedures;
        this.preferredHospitalIds = preferredHospitalIds;
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
        this.accommodationPreference = accommodationPreference;
        this.conciergeServices = conciergeServices;
        this.budgetMin = budgetMin;
        this.budgetMax = budgetMax;
        this.budgetCurrency = budgetCurrency;
        this.additionalRequests = additionalRequests;
        this.status = ProposalRequestStatus.PENDING;
    }
}
