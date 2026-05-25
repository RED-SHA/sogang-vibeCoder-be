package com.kmedical.control;

import com.kmedical.domain.entity.Agency;
import com.kmedical.dto.agency.AgencyDTO;
import com.kmedical.util.AuditLogger;
import com.kmedical.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * SRV-C05 — AgencyController
 * 책임: 에이전시 프로필·라이선스·포트폴리오 관리.
 * UC: UC-A12, UC-P06
 * NFR 적용: ConcurrentHashMap, 라이선스번호·이메일·HTTPS URL 검증
 */
public class AgencyController {

    private final Map<String, Agency> agencyStore = new ConcurrentHashMap<>();

    private void guardNotClosedDown() {
        if (SystemStateRegistry.getInstance().isClosedDown()) {
            AuditLogger.closedDownAccess("AgencyController", "UNKNOWN");
            throw new IllegalStateException("System is closed down. Customer operations are not permitted.");
        }
    }

    /**
     * 에이전시 프로필을 조회한다.
     */
    public AgencyDTO getAgency(String agencyId) {
        guardNotClosedDown();
        return toDTO(findAgency(agencyId));
    }

    /**
     * 에이전시 프로필 및 라이선스 정보를 저장한다.
     * 검증: nameKo/nameEn/licenseNumber/contactEmail 필수, HTTPS URL, 이메일 형식
     */
    public AgencyDTO saveAgency(AgencyDTO dto) {
        guardNotClosedDown();
        ValidationUtil.requireNotNull(dto, "AgencyDTO");
        ValidationUtil.requireLengthBetween(dto.getNameKo(), 1, 100, "nameKo");
        ValidationUtil.requireLengthBetween(dto.getNameEn(), 2, 100, "nameEn");
        ValidationUtil.requireValidLicenseNumber(dto.getLicenseNumber());
        ValidationUtil.requireValidEmail(dto.getContactEmail());
        if (dto.getLicenseDocumentUrl() != null && !dto.getLicenseDocumentUrl().isEmpty()) {
            ValidationUtil.requireHttpsUrl(dto.getLicenseDocumentUrl(), "licenseDocumentUrl");
        }

        Agency agency;
        if (dto.getAgencyId() != null && agencyStore.containsKey(dto.getAgencyId())) {
            agency = agencyStore.get(dto.getAgencyId());
        } else {
            agency = new Agency();
            agency.setAgencyId(UUID.randomUUID().toString());
            agency.setIsVerified(false);
        }

        agency.setNameKo(dto.getNameKo());
        agency.setNameEn(dto.getNameEn());
        agency.setLicenseNumber(dto.getLicenseNumber());
        agency.setLicenseDocumentUrl(dto.getLicenseDocumentUrl());
        agency.setPortfolioItems(dto.getPortfolioItems());
        agency.setContactEmail(dto.getContactEmail());
        agency.setUpdatedAt(LocalDateTime.now());

        agencyStore.put(agency.getAgencyId(), agency);
        return toDTO(agency);
    }

    /**
     * 에이전시 라이선스 인증 상태를 갱신한다.
     * isVerified=true 설정은 이 메서드를 통해서만 허용.
     */
    public AgencyDTO verifyAgency(String agencyId, boolean verified) {
        guardNotClosedDown();
        ValidationUtil.requireNotBlank(agencyId, "agencyId");
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
