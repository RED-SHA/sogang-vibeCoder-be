package com.kmedical.control;

import com.kmedical.domain.entity.Agency;
import com.kmedical.dto.agency.AgencyDTO;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * SRV-C05 — AgencyController
 * 책임: 에이전시 프로필·라이선스·포트폴리오 관리.
 * UC: UC-A12, UC-P06
 */
public class AgencyController {

    private final Map<String, Agency> agencyStore = new HashMap<>();

    private void guardNotClosedDown() {
        if (SystemStateRegistry.getInstance().isClosedDown()) {
            throw new IllegalStateException("System is closed down. Customer operations are not permitted.");
        }
    }

    /**
     * 에이전시 프로필을 조회한다.
     * System Response: agencyId 검증 → Agency 조회 → DTO 반환
     */
    public AgencyDTO getAgency(String agencyId) {
        guardNotClosedDown();
        Agency agency = findAgency(agencyId);
        return toDTO(agency);
    }

    /**
     * 에이전시 프로필 및 라이선스 정보를 저장한다.
     * System Response: 입력 검증 → Agency 저장 → DTO 반환
     */
    public AgencyDTO saveAgency(AgencyDTO dto) {
        guardNotClosedDown();
        if (dto == null) throw new IllegalArgumentException("Agency data is null.");

        Agency agency;
        if (dto.getAgencyId() != null && agencyStore.containsKey(dto.getAgencyId())) {
            agency = agencyStore.get(dto.getAgencyId());
        } else {
            agency = new Agency();
            agency.setAgencyId(UUID.randomUUID().toString());
        }

        agency.setNameKo(dto.getNameKo());
        agency.setNameEn(dto.getNameEn());
        agency.setLicenseNumber(dto.getLicenseNumber());
        agency.setLicenseDocumentUrl(dto.getLicenseDocumentUrl());
        agency.setIsVerified(dto.getIsVerified());
        agency.setPortfolioItems(dto.getPortfolioItems());
        agency.setContactEmail(dto.getContactEmail());
        agency.setUpdatedAt(LocalDateTime.now());

        agencyStore.put(agency.getAgencyId(), agency);
        return toDTO(agency);
    }

    /**
     * 에이전시 라이선스 인증 상태를 갱신한다.
     */
    public AgencyDTO verifyAgency(String agencyId, boolean verified) {
        guardNotClosedDown();
        Agency agency = findAgency(agencyId);
        agency.setIsVerified(verified);
        agency.setUpdatedAt(LocalDateTime.now());
        return toDTO(agency);
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private Agency findAgency(String agencyId) {
        Agency a = agencyStore.get(agencyId);
        if (a == null) throw new IllegalArgumentException("Agency not found: " + agencyId);
        return a;
    }

    private AgencyDTO toDTO(Agency a) {
        AgencyDTO dto = new AgencyDTO();
        dto.setAgencyId(a.getAgencyId());
        dto.setNameKo(a.getNameKo());
        dto.setNameEn(a.getNameEn());
        dto.setLicenseNumber(a.getLicenseNumber());
        dto.setLicenseDocumentUrl(a.getLicenseDocumentUrl());
        dto.setIsVerified(a.getIsVerified());
        dto.setPortfolioItems(a.getPortfolioItems());
        dto.setContactEmail(a.getContactEmail());
        dto.setUpdatedAt(a.getUpdatedAt());
        return dto;
    }
}
