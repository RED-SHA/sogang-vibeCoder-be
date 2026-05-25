package com.kmedical.control;

import com.kmedical.domain.entity.PatientJourney;
import com.kmedical.domain.entity.ScheduleItem;
import com.kmedical.domain.enums.JourneyStatus;
import com.kmedical.domain.enums.ScheduleItemStatus;
import com.kmedical.dto.journey.PatientJourneyDTO;
import com.kmedical.dto.journey.ScheduleItemDTO;
import com.kmedical.dto.journey.ScheduleItemUpdateRequestDTO;
import com.kmedical.util.AuditLogger;
import com.kmedical.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SRV-C08 — JourneyController
 * 책임: 여정 생성, 일정 관리, 상태 동기화, 좌표 제공.
 * UC: UC-A06, UC-P08, UC-P10, UC-P11, UC-S04, UC-S07
 * 제약: ScheduleItem 상태 전이 SCHEDULED→IN_PROGRESS→COMPLETED 순서만 허용
 * NFR 적용: ConcurrentHashMap, CopyOnWriteArrayList, 좌표 범위 검증, scheduledEndAt > scheduledStartAt
 */
public class JourneyController {

    private final Map<String, PatientJourney> journeyStore = new ConcurrentHashMap<>();
    private final Map<String, List<ScheduleItem>> scheduleStore = new ConcurrentHashMap<>();
    private final AlertController alertController;

    public JourneyController(AlertController alertController) {
        this.alertController = alertController;
    }

    private void guardNotClosedDown() {
        if (SystemStateRegistry.getInstance().isClosedDown()) {
            AuditLogger.closedDownAccess("JourneyController", "UNKNOWN");
            throw new IllegalStateException("System is closed down. Customer operations are not permitted.");
        }
    }

    /**
     * 확정 견적을 기반으로 환자 여정을 생성한다.
     * System Response: Quotation ACCEPTED 확인 → PatientJourney 생성(SCHEDULED)
     */
    public PatientJourneyDTO createJourney(PatientJourneyDTO dto) {
        guardNotClosedDown();
        ValidationUtil.requireNotNull(dto, "PatientJourneyDTO");
        ValidationUtil.requireNotBlank(dto.getPatientId(), "patientId");

        PatientJourney journey = new PatientJourney();
        journey.setPatientJourneyId(UUID.randomUUID().toString());
        journey.setPatientId(dto.getPatientId());
        journey.setAgencyId(dto.getAgencyId());
        journey.setQuotationId(dto.getQuotationId());
        journey.setItineraryTemplateId(dto.getItineraryTemplateId());
        journey.setStatus(JourneyStatus.SCHEDULED);
        journey.setArrivalDate(dto.getArrivalDate());
        journey.setDepartureDate(dto.getDepartureDate());
        journey.setCreatedAt(LocalDateTime.now());
        journey.setUpdatedAt(LocalDateTime.now());

        journeyStore.put(journey.getPatientJourneyId(), journey);
        scheduleStore.put(journey.getPatientJourneyId(), new CopyOnWriteArrayList<>());
        return toJourneyDTO(journey);
    }

    /**
     * 환자 여정을 조회한다.
     */
    public PatientJourneyDTO getJourney(String journeyId) {
        guardNotClosedDown();
        ValidationUtil.requireNotBlank(journeyId, "journeyId");
        return toJourneyDTO(findJourney(journeyId));
    }

    /**
     * 환자의 여정 목록을 조회한다.
     */
    public List<PatientJourneyDTO> getJourneysByPatient(String patientId) {
        guardNotClosedDown();
        ValidationUtil.requireNotBlank(patientId, "patientId");
        List<PatientJourneyDTO> result = new ArrayList<>();
        for (PatientJourney j : journeyStore.values()) {
            if (patientId.equals(j.getPatientId())) result.add(toJourneyDTO(j));
        }
        return result;
    }

    /**
     * 일정 항목을 수정한다.
     * System Response: ScheduleItem 변경 → isCritical 확인 시 AlertController로 알림 위임 (UC-E01 extend)
     */
    public ScheduleItemDTO updateScheduleItem(ScheduleItemUpdateRequestDTO request) {
        guardNotClosedDown();
        ValidationUtil.requireNotNull(request, "ScheduleItemUpdateRequestDTO");
        ValidationUtil.requireNotBlank(request.getScheduleItemId(), "scheduleItemId");

        ScheduleItem item = findScheduleItem(request.getScheduleItemId());

        if (request.getStatus() != null) {
            validateStatusTransition(item.getStatus(), request.getStatus());
            item.setStatus(request.getStatus());
        }
        if (request.getTitle() != null) {
            ValidationUtil.requireLengthBetween(request.getTitle(), 1, 200, "title");
            item.setTitle(request.getTitle());
        }
        if (request.getScheduledStartAt() != null) item.setScheduledStartAt(request.getScheduledStartAt());
        if (request.getScheduledEndAt() != null) item.setScheduledEndAt(request.getScheduledEndAt());
        if (request.getLocationAddressEn() != null) item.setLocationAddressEn(request.getLocationAddressEn());
        if (request.getLocationCoordLat() != null) {
            ValidationUtil.requireLatitude(request.getLocationCoordLat());
            item.setLocationCoordLat(request.getLocationCoordLat());
        }
        if (request.getLocationCoordLng() != null) {
            ValidationUtil.requireLongitude(request.getLocationCoordLng());
            item.setLocationCoordLng(request.getLocationCoordLng());
        }
        if (request.getIsCritical() != null) item.setIsCritical(request.getIsCritical());
        if (request.getMemo() != null) {
            ValidationUtil.requireMaxLength(request.getMemo(), 1000, "memo");
            item.setMemo(request.getMemo());
        }

        // scheduledEndAt > scheduledStartAt 보장
        if (item.getScheduledStartAt() != null && item.getScheduledEndAt() != null) {
            ValidationUtil.requireEndAfterStart(item.getScheduledStartAt(), item.getScheduledEndAt());
        }

        // UC-E01 extend: 주요 일정 변경 시 AlertController 위임
        if (Boolean.TRUE.equals(item.getIsCritical())) {
            alertController.notifyScheduleChange(item.getScheduleItemId());
        }

        return toItemDTO(item);
    }

    /**
     * 일정 항목을 추가한다.
     * System Response: 여정 존재 확인 → ScheduleItem 저장
     */
    public ScheduleItemDTO addScheduleItem(ScheduleItemDTO dto) {
        guardNotClosedDown();
        ValidationUtil.requireNotNull(dto, "ScheduleItemDTO");
        ValidationUtil.requireNotBlank(dto.getPatientJourneyId(), "patientJourneyId");

        findJourney(dto.getPatientJourneyId());

        if (dto.getTitle() != null) ValidationUtil.requireLengthBetween(dto.getTitle(), 1, 200, "title");
        if (dto.getSortOrder() != null && dto.getSortOrder() < 0)
            throw new IllegalArgumentException("sortOrder must be >= 0.");
        if (dto.getMemo() != null) ValidationUtil.requireMaxLength(dto.getMemo(), 1000, "memo");
        if (dto.getLocationCoordLat() != null) ValidationUtil.requireLatitude(dto.getLocationCoordLat());
        if (dto.getLocationCoordLng() != null) ValidationUtil.requireLongitude(dto.getLocationCoordLng());
        if (dto.getScheduledStartAt() != null && dto.getScheduledEndAt() != null) {
            ValidationUtil.requireEndAfterStart(dto.getScheduledStartAt(), dto.getScheduledEndAt());
        }

        ScheduleItem item = new ScheduleItem();
        item.setScheduleItemId(UUID.randomUUID().toString());
        item.setPatientJourneyId(dto.getPatientJourneyId());
        item.setItemType(dto.getItemType());
        item.setTitle(dto.getTitle());
        item.setScheduledStartAt(dto.getScheduledStartAt());
        item.setScheduledEndAt(dto.getScheduledEndAt());
        item.setLocationAddressEn(dto.getLocationAddressEn());
        item.setLocationCoordLat(dto.getLocationCoordLat());
        item.setLocationCoordLng(dto.getLocationCoordLng());
        item.setStatus(ScheduleItemStatus.SCHEDULED);
        item.setIsCritical(dto.getIsCritical());
        item.setMemo(dto.getMemo());
        item.setSortOrder(dto.getSortOrder());

        scheduleStore.computeIfAbsent(dto.getPatientJourneyId(), k -> new CopyOnWriteArrayList<>()).add(item);
        return toItemDTO(item);
    }

    /**
     * 여정의 일정 목록을 조회한다.
     */
    public List<ScheduleItemDTO> getScheduleItems(String journeyId) {
        guardNotClosedDown();
        ValidationUtil.requireNotBlank(journeyId, "journeyId");
        findJourney(journeyId);
        List<ScheduleItemDTO> result = new ArrayList<>();
        for (ScheduleItem i : scheduleStore.getOrDefault(journeyId, new ArrayList<>())) {
            result.add(toItemDTO(i));
        }
        return result;
    }

    /**
     * 에이전시 소속 진행 중 여정 목록을 조회한다 (대시보드용).
     */
    public List<PatientJourneyDTO> getActiveJourneysByAgency(String agencyId) {
        guardNotClosedDown();
        ValidationUtil.requireNotBlank(agencyId, "agencyId");
        List<PatientJourneyDTO> result = new ArrayList<>();
        for (PatientJourney j : journeyStore.values()) {
            if (agencyId.equals(j.getAgencyId())
                    && j.getStatus() != JourneyStatus.COMPLETED
                    && j.getStatus() != JourneyStatus.CANCELLED) {
                result.add(toJourneyDTO(j));
            }
        }
        return result;
    }

    /**
     * 경로 안내용 일정 항목 좌표를 제공한다 (UC-P11, UC-S07).
     */
    public ScheduleItemDTO getScheduleItemForNavigation(String scheduleItemId) {
        guardNotClosedDown();
        ValidationUtil.requireNotBlank(scheduleItemId, "scheduleItemId");
        return toItemDTO(findScheduleItem(scheduleItemId));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void validateStatusTransition(ScheduleItemStatus current, ScheduleItemStatus next) {
        if (current == ScheduleItemStatus.SCHEDULED && next == ScheduleItemStatus.IN_PROGRESS) return;
        if (current == ScheduleItemStatus.IN_PROGRESS && next == ScheduleItemStatus.COMPLETED) return;
        if (current == next) return;
        throw new IllegalStateException(
                "Invalid status transition: " + current + " → " + next +
                        ". Allowed: SCHEDULED→IN_PROGRESS→COMPLETED.");
    }

    private PatientJourney findJourney(String id) {
        PatientJourney j = journeyStore.get(id);
        if (j == null) throw new IllegalArgumentException("PatientJourney not found: " + id);
        return j;
    }

    private ScheduleItem findScheduleItem(String id) {
        for (List<ScheduleItem> items : scheduleStore.values()) {
            for (ScheduleItem item : items) {
                if (item.getScheduleItemId().equals(id)) return item;
            }
        }
        throw new IllegalArgumentException("ScheduleItem not found: " + id);
    }

    private PatientJourneyDTO toJourneyDTO(PatientJourney j) {
        PatientJourneyDTO dto = new PatientJourneyDTO();
        dto.setPatientJourneyId(j.getPatientJourneyId());
        dto.setPatientId(j.getPatientId());
        dto.setAgencyId(j.getAgencyId());
        dto.setQuotationId(j.getQuotationId());
        dto.setItineraryTemplateId(j.getItineraryTemplateId());
        dto.setStatus(j.getStatus());
        dto.setArrivalDate(j.getArrivalDate());
        dto.setDepartureDate(j.getDepartureDate());
        dto.setCreatedAt(j.getCreatedAt());
        dto.setUpdatedAt(j.getUpdatedAt());
        return dto;
    }

    private ScheduleItemDTO toItemDTO(ScheduleItem i) {
        ScheduleItemDTO dto = new ScheduleItemDTO();
        dto.setScheduleItemId(i.getScheduleItemId());
        dto.setPatientJourneyId(i.getPatientJourneyId());
        dto.setItemType(i.getItemType());
        dto.setTitle(i.getTitle());
        dto.setScheduledStartAt(i.getScheduledStartAt());
        dto.setScheduledEndAt(i.getScheduledEndAt());
        dto.setLocationAddressEn(i.getLocationAddressEn());
        dto.setLocationCoordLat(i.getLocationCoordLat());
        dto.setLocationCoordLng(i.getLocationCoordLng());
        dto.setStatus(i.getStatus());
        dto.setIsCritical(i.getIsCritical());
        dto.setMemo(i.getMemo());
        dto.setSortOrder(i.getSortOrder());
        return dto;
    }
}
