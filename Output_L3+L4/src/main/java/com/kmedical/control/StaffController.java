package com.kmedical.control;

import com.kmedical.domain.entity.Chauffeur;
import com.kmedical.domain.entity.Interpreter;
import com.kmedical.domain.entity.Staff;
import com.kmedical.domain.enums.StaffAvailability;
import com.kmedical.dto.staff.StaffDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SRV-C09 — StaffController
 * 책임: 스태프 프로필·가용 상태 관리.
 * UC: UC-A08, UC-S02
 */
public class StaffController {

    private final Map<String, Staff> staffStore = new HashMap<>();

    private void guardNotClosedDown() {
        if (SystemStateRegistry.getInstance().isClosedDown()) {
            throw new IllegalStateException("System is closed down. Customer operations are not permitted.");
        }
    }

    /**
     * 스태프 프로필을 조회한다.
     * System Response: staffId 검증 → Staff 조회 → DTO 반환
     */
    public StaffDTO getStaff(String staffId) {
        guardNotClosedDown();
        return toDTO(findStaff(staffId));
    }

    /**
     * 스태프 프로필을 수정한다.
     * System Response: 입력 검증 → Staff 속성 갱신 → DTO 반환
     */
    public StaffDTO updateProfile(StaffDTO dto) {
        guardNotClosedDown();
        if (dto == null || dto.getUserId() == null) {
            throw new IllegalArgumentException("Staff profile data is incomplete.");
        }

        Staff staff = findStaff(dto.getUserId());
        staff.setDisplayNameEn(dto.getDisplayNameEn());
        staff.setSpecialization(dto.getSpecialization());
        staff.setProfilePhotoUrl(dto.getProfilePhotoUrl());
        staff.setExperienceYears(dto.getExperienceYears());

        if (staff instanceof Chauffeur && dto.getVehicleNumber() != null) {
            ((Chauffeur) staff).setVehicleNumber(dto.getVehicleNumber());
            ((Chauffeur) staff).setVehicleType(dto.getVehicleType());
        }
        if (staff instanceof Interpreter && dto.getInterpreterLanguages() != null) {
            ((Interpreter) staff).setLanguages(dto.getInterpreterLanguages());
        }

        return toDTO(staff);
    }

    /**
     * 스태프의 가용 상태를 변경한다.
     * System Response: 상태 검증 → availabilityStatus 갱신
     */
    public StaffDTO updateAvailability(String staffId, StaffAvailability status) {
        guardNotClosedDown();
        if (status == null) throw new IllegalArgumentException("Availability status is null.");
        Staff staff = findStaff(staffId);
        staff.setAvailabilityStatus(status);
        return toDTO(staff);
    }

    /**
     * 에이전시 소속 스태프 목록을 조회한다.
     */
    public List<StaffDTO> getStaffByAgency(String agencyId) {
        guardNotClosedDown();
        List<StaffDTO> result = new ArrayList<>();
        for (Staff s : staffStore.values()) {
            if (agencyId.equals(s.getAgencyId())) result.add(toDTO(s));
        }
        return result;
    }

    public void registerStaff(Staff staff) {
        staffStore.put(staff.getUserId(), staff);
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private Staff findStaff(String id) {
        Staff s = staffStore.get(id);
        if (s == null) throw new IllegalArgumentException("Staff not found: " + id);
        return s;
    }

    private StaffDTO toDTO(Staff s) {
        StaffDTO dto = new StaffDTO();
        dto.setUserId(s.getUserId());
        dto.setAgencyId(s.getAgencyId());
        dto.setDisplayNameEn(s.getDisplayNameEn());
        dto.setSpecialization(s.getSpecialization());
        dto.setProfilePhotoUrl(s.getProfilePhotoUrl());
        dto.setExperienceYears(s.getExperienceYears());
        dto.setAvailabilityStatus(s.getAvailabilityStatus());
        dto.setOnboardingComplete(s.getOnboardingComplete());
        if (s instanceof Chauffeur) {
            dto.setStaffType("CHAUFFEUR");
            dto.setVehicleNumber(((Chauffeur) s).getVehicleNumber());
            dto.setVehicleType(((Chauffeur) s).getVehicleType());
        } else if (s instanceof Interpreter) {
            dto.setStaffType("INTERPRETER");
            dto.setInterpreterLanguages(((Interpreter) s).getLanguages());
        }
        return dto;
    }
}
