package com.kmedical.control;

import com.kmedical.adapter.PushAdapter;
import com.kmedical.adapter.RealtimeSyncAdapter;
import com.kmedical.domain.entity.PatientJourney;
import com.kmedical.domain.entity.ScheduleItem;
import com.kmedical.domain.enums.EditItineraryErrorCode;
import com.kmedical.domain.enums.EditItineraryStatus;
import com.kmedical.domain.enums.JourneyStatus;
import com.kmedical.domain.enums.ScheduleItemStatus;
import com.kmedical.dto.journey.EditItineraryRequestDTO;
import com.kmedical.dto.journey.EditItineraryResponseDTO;
import com.kmedical.dto.journey.PatientJourneyDTO;
import com.kmedical.dto.journey.ScheduleItemDTO;
import com.kmedical.dto.journey.ScheduleItemUpdateRequestDTO;
import com.kmedical.dto.staff.StaffAssignmentCreateRequestDTO;
import com.kmedical.dto.staff.StaffAssignmentDTO;
import com.kmedical.util.AuditLogger;
import com.kmedical.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
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
    private final Map<String, EditLock> editLocks = new ConcurrentHashMap<>();
    private final AlertController alertController;
    private final StaffAssignmentController staffAssignmentController;
    private final RealtimeSyncAdapter realtimeSyncAdapter;
    private final PushAdapter pushAdapter;

    public JourneyController(AlertController alertController) {
        this(alertController, null, snapshot -> true, null);
    }

    public JourneyController(AlertController alertController,
                             StaffAssignmentController staffAssignmentController,
                             RealtimeSyncAdapter realtimeSyncAdapter,
                             PushAdapter pushAdapter) {
        this.alertController = alertController;
        this.staffAssignmentController = staffAssignmentController;
        this.realtimeSyncAdapter = realtimeSyncAdapter;
        this.pushAdapter = pushAdapter;
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
    /**
     * UC-ADM-07 Edit Itinerary L5 flow.
     */
    public EditItineraryResponseDTO editItinerary(EditItineraryRequestDTO request) {
        guardNotClosedDown();
        ValidationUtil.requireNotNull(request, "EditItineraryRequestDTO");
        EditItineraryStatus state = EditItineraryStatus.INIT;

        // L5 1.1 / A6.2: authenticate/RBAC gate represented by operator identity.
        if (request.getOperatorId() == null || request.getOperatorId().isBlank()) {
            AuditLogger.log("ITINERARY_EDIT_REJECTED", "UNKNOWN", request.getScheduleItemId(), false,
                    "reason=PERMISSION_DENIED");
            return EditItineraryResponseDTO.error(EditItineraryErrorCode.PERMISSION_DENIED,
                    "operatorId is required.", EditItineraryStatus.REJECTED);
        }
        state = transition(state, "authOk", true, EditItineraryStatus.AUTH_CHECKED);

        // L5 2.1: selected schedule item.
        ValidationUtil.requireNotBlank(request.getScheduleItemId(), "scheduleItemId");
        ScheduleItem selected = findScheduleItem(request.getScheduleItemId());
        if (request.getPatientJourneyId() != null
                && !request.getPatientJourneyId().equals(selected.getPatientJourneyId())) {
            return EditItineraryResponseDTO.validationFailed("patientJourneyId does not match schedule item.",
                    toItemDTO(selected));
        }
        state = transition(state, "selectItem", true, EditItineraryStatus.ITEM_SELECTED);

        EditLock lock = acquireEditLock(request.getScheduleItemId(), request.getOperatorId());
        if (lock == null) {
            EditLock holder = editLocks.get(request.getScheduleItemId());
            return EditItineraryResponseDTO.lockHeld(holder != null ? holder.holderId : null); // L5 3.A7.1
        }
        state = transition(state, "lockAcquired", true, EditItineraryStatus.LOCK_ACQUIRED); // L5 3.1

        try {
            ScheduleItem before;
            ScheduleItemDTO submittedView;
            ScheduleItemDTO updatedSnapshot;
            List<StaffAssignmentDTO> assignments;

            synchronized (lock.monitor) {
                // L5 4.1: load editable form snapshot while holding the edit lock.
                ScheduleItem item = findScheduleItem(request.getScheduleItemId());
                before = copyItem(item);
                state = transition(state, "loadEditableForm", true, EditItineraryStatus.EDIT_FORM_RETURNED);

                if (request.isCancelRequested()) {
                    transition(state, "cancel", true, EditItineraryStatus.CANCELLED);
                    return EditItineraryResponseDTO.error(EditItineraryErrorCode.EDIT_CANCELLED,
                            "edit_cancelled", EditItineraryStatus.CANCELLED); // L5 A5.3
                }

                ScheduleItemUpdateRequestDTO update = normalizedUpdate(request);
                submittedView = buildSubmittedView(before, update);
                state = transition(state, "submit", true, EditItineraryStatus.SUBMITTED); // L5 5.2

                try {
                    validateEditPayload(item, update); // L5 6.1 / 6.3
                } catch (IllegalArgumentException | IllegalStateException e) {
                    return EditItineraryResponseDTO.validationFailed(e.getMessage(), submittedView); // L5 A1
                }
                state = transition(state, "validationOk", true, EditItineraryStatus.VALIDATED);

                // L5 7.2: optimistic version comparison before mutation.
                if (request.getExpectedVersion() == null || item.getVersion() != request.getExpectedVersion()) {
                    return EditItineraryResponseDTO.versionConflict(toItemDTO(item), submittedView); // L5 7.A2.1~7.A2.3
                }

                applyScheduleUpdate(item, update); // L5 7.1
                item.incrementVersion(); // L5 8.1

                // L5 9.1 / 9.2: assigned staff association update.
                assignments = replaceAssignments(request, item.getScheduleItemId());

                updatedSnapshot = toItemDTO(item);
                state = transition(state, "persistOk", true, EditItineraryStatus.PERSISTED);

                // L5 10.1: audit log with before/after diff.
                AuditLogger.log("ITINERARY_EDITED", request.getOperatorId(), item.getScheduleItemId(), true,
                        computeDiffDetail(before, item));
                state = transition(state, "auditLogged", true, EditItineraryStatus.AUDIT_LOGGED);
            }

            // L5 11.1 and 12.3 are independent outputs from the same post-commit state.
            state = transition(state, "requestSync", true, EditItineraryStatus.SYNC_REQUESTED);
            CompletableFuture<Boolean> syncFuture =
                    CompletableFuture.supplyAsync(() -> publishRealtimeSnapshot(updatedSnapshot)); // concurrent L5 11.1
            CompletableFuture<Boolean> pushFuture =
                    CompletableFuture.supplyAsync(() -> sendItineraryPush(assignments, updatedSnapshot)); // concurrent L5 12.3

            boolean syncOk = syncFuture.join();
            boolean pushOk = pushFuture.join();
            boolean degradedSync = !syncOk;
            boolean pushWarning = !pushOk;

            if (degradedSync) {
                AuditLogger.warn("REALTIME_SYNC_DEGRADED",
                        "scheduleItemId=" + updatedSnapshot.getScheduleItemId()); // L5 11.A3.2
                state = transition(state, "syncFailedAfterRetry", true, EditItineraryStatus.SYNC_DEGRADED);
            }
            if (pushWarning) {
                AuditLogger.warn("PUSH_NOTIFICATION_FAILED",
                        "scheduleItemId=" + updatedSnapshot.getScheduleItemId()); // L5 12.A4.2
            }

            state = transition(state, "returnUpdatedView", true, EditItineraryStatus.READY); // L5 14.1 / 15.1
            return EditItineraryResponseDTO.success(updatedSnapshot, assignments, degradedSync, pushWarning);
        } finally {
            releaseEditLock(request.getScheduleItemId(), request.getOperatorId()); // L5 13.1 / A5.2
        }
    }

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
        item.setVersion(0);

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

    private EditItineraryStatus transition(EditItineraryStatus current,
                                           String event,
                                           boolean guard,
                                           EditItineraryStatus next) {
        if (!guard || !isAllowedTransition(current, next)) {
            throw new IllegalStateException("Invalid edit itinerary transition: "
                    + current + " --" + event + "--> " + next);
        }
        return next;
    }

    private boolean isAllowedTransition(EditItineraryStatus current, EditItineraryStatus next) {
        if (current == next) return true;
        if (next == EditItineraryStatus.FAILED) return true;
        if (current == EditItineraryStatus.INIT) {
            return next == EditItineraryStatus.AUTH_CHECKED || next == EditItineraryStatus.REJECTED;
        }
        if (current == EditItineraryStatus.AUTH_CHECKED) return next == EditItineraryStatus.ITEM_SELECTED;
        if (current == EditItineraryStatus.ITEM_SELECTED) {
            return next == EditItineraryStatus.LOCK_ACQUIRED || next == EditItineraryStatus.REJECTED;
        }
        if (current == EditItineraryStatus.LOCK_ACQUIRED) return next == EditItineraryStatus.EDIT_FORM_RETURNED;
        if (current == EditItineraryStatus.EDIT_FORM_RETURNED) {
            return next == EditItineraryStatus.SUBMITTED || next == EditItineraryStatus.CANCELLED;
        }
        if (current == EditItineraryStatus.SUBMITTED) {
            return next == EditItineraryStatus.VALIDATED || next == EditItineraryStatus.EDIT_FORM_RETURNED;
        }
        if (current == EditItineraryStatus.VALIDATED) {
            return next == EditItineraryStatus.PERSISTED || next == EditItineraryStatus.REJECTED;
        }
        if (current == EditItineraryStatus.PERSISTED) return next == EditItineraryStatus.AUDIT_LOGGED;
        if (current == EditItineraryStatus.AUDIT_LOGGED) return next == EditItineraryStatus.SYNC_REQUESTED;
        if (current == EditItineraryStatus.SYNC_REQUESTED) {
            return next == EditItineraryStatus.SYNC_DEGRADED || next == EditItineraryStatus.READY;
        }
        if (current == EditItineraryStatus.SYNC_DEGRADED) return next == EditItineraryStatus.READY;
        return false;
    }

    private EditLock acquireEditLock(String scheduleItemId, String operatorId) {
        EditLock candidate = new EditLock(operatorId);
        EditLock existing = editLocks.putIfAbsent(scheduleItemId, candidate);
        if (existing == null) return candidate;
        if (operatorId.equals(existing.holderId)) return existing;
        return null;
    }

    private void releaseEditLock(String scheduleItemId, String operatorId) {
        EditLock current = editLocks.get(scheduleItemId);
        if (current != null && operatorId.equals(current.holderId)) {
            editLocks.remove(scheduleItemId, current);
        }
    }

    private ScheduleItemUpdateRequestDTO normalizedUpdate(EditItineraryRequestDTO request) {
        ScheduleItemUpdateRequestDTO update = request.getUpdate();
        if (update == null) update = new ScheduleItemUpdateRequestDTO();
        update.setScheduleItemId(request.getScheduleItemId());
        return update;
    }

    private void validateEditPayload(ScheduleItem current, ScheduleItemUpdateRequestDTO update) {
        if (update.getStatus() != null) validateStatusTransition(current.getStatus(), update.getStatus());
        if (update.getTitle() != null) ValidationUtil.requireLengthBetween(update.getTitle(), 1, 200, "title");
        if (update.getLocationCoordLat() != null) ValidationUtil.requireLatitude(update.getLocationCoordLat());
        if (update.getLocationCoordLng() != null) ValidationUtil.requireLongitude(update.getLocationCoordLng());
        if (update.getMemo() != null) ValidationUtil.requireMaxLength(update.getMemo(), 1000, "memo");

        ScheduleItem proposed = copyItem(current);
        applyScheduleUpdate(proposed, update);
        if (proposed.getScheduledStartAt() != null && proposed.getScheduledEndAt() != null) {
            ValidationUtil.requireEndAfterStart(proposed.getScheduledStartAt(), proposed.getScheduledEndAt());
            if (proposed.getScheduledStartAt().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("scheduledStartAt must not be in the past.");
            }
            validateNoSiblingTimeOverlap(proposed);
        }
    }

    private void validateNoSiblingTimeOverlap(ScheduleItem proposed) {
        List<ScheduleItem> siblings = scheduleStore.getOrDefault(proposed.getPatientJourneyId(), new ArrayList<>());
        for (ScheduleItem sibling : siblings) {
            if (proposed.getScheduleItemId().equals(sibling.getScheduleItemId())) continue;
            if (sibling.getScheduledStartAt() == null || sibling.getScheduledEndAt() == null) continue;
            boolean overlaps = proposed.getScheduledStartAt().isBefore(sibling.getScheduledEndAt())
                    && proposed.getScheduledEndAt().isAfter(sibling.getScheduledStartAt());
            if (overlaps) {
                throw new IllegalArgumentException("schedule time overlaps sibling item: "
                        + sibling.getScheduleItemId());
            }
        }
    }

    private void applyScheduleUpdate(ScheduleItem item, ScheduleItemUpdateRequestDTO update) {
        if (update.getItemType() != null) item.setItemType(update.getItemType());
        if (update.getStatus() != null) item.setStatus(update.getStatus());
        if (update.getTitle() != null) item.setTitle(update.getTitle());
        if (update.getScheduledStartAt() != null) item.setScheduledStartAt(update.getScheduledStartAt());
        if (update.getScheduledEndAt() != null) item.setScheduledEndAt(update.getScheduledEndAt());
        if (update.getLocationAddressEn() != null) item.setLocationAddressEn(update.getLocationAddressEn());
        if (update.getLocationCoordLat() != null) item.setLocationCoordLat(update.getLocationCoordLat());
        if (update.getLocationCoordLng() != null) item.setLocationCoordLng(update.getLocationCoordLng());
        if (update.getIsCritical() != null) item.setIsCritical(update.getIsCritical());
        if (update.getMemo() != null) item.setMemo(update.getMemo());
    }

    private ScheduleItemDTO buildSubmittedView(ScheduleItem before, ScheduleItemUpdateRequestDTO update) {
        ScheduleItem submitted = copyItem(before);
        applyScheduleUpdate(submitted, update);
        return toItemDTO(submitted);
    }

    private List<StaffAssignmentDTO> replaceAssignments(EditItineraryRequestDTO request, String scheduleItemId) {
        if (staffAssignmentController == null) return new ArrayList<>();
        return staffAssignmentController.replaceAssignmentsForScheduleItem(
                scheduleItemId, request.getStaffAssignments(), request.getOperatorId());
    }

    private boolean publishRealtimeSnapshot(ScheduleItemDTO snapshot) {
        try {
            return realtimeSyncAdapter == null || realtimeSyncAdapter.publishScheduleSnapshot(snapshot);
        } catch (Exception e) {
            AuditLogger.warn("REALTIME_SYNC_FAILED", e.getMessage());
            return false;
        }
    }

    private boolean sendItineraryPush(List<StaffAssignmentDTO> assignments, ScheduleItemDTO updatedSnapshot) {
        if (pushAdapter == null) return true;
        try {
            List<String> staffIds = new ArrayList<>();
            for (StaffAssignmentDTO assignment : assignments) {
                staffIds.add(assignment.getStaffId());
            }
            if (staffIds.isEmpty()) return true;
            String endpoint = resolvePatientMagicLinkEndpoint(updatedSnapshot); // L5 12.2
            return pushAdapter.sendBulkPush(staffIds, "Itinerary Updated",
                    "Schedule item updated: " + endpoint);
        } catch (Exception e) {
            AuditLogger.warn("PUSH_NOTIFICATION_EXCEPTION", e.getMessage());
            return false;
        }
    }

    private String resolvePatientMagicLinkEndpoint(ScheduleItemDTO item) {
        return "/patient/journeys/" + item.getPatientJourneyId() + "/itinerary";
    }

    private String computeDiffDetail(ScheduleItem before, ScheduleItem after) {
        List<String> diff = new ArrayList<>();
        appendDiff(diff, "/itemType", before.getItemType(), after.getItemType());
        appendDiff(diff, "/title", before.getTitle(), after.getTitle());
        appendDiff(diff, "/scheduledStartAt", before.getScheduledStartAt(), after.getScheduledStartAt());
        appendDiff(diff, "/scheduledEndAt", before.getScheduledEndAt(), after.getScheduledEndAt());
        appendDiff(diff, "/locationAddressEn", before.getLocationAddressEn(), after.getLocationAddressEn());
        appendDiff(diff, "/locationCoordLat", before.getLocationCoordLat(), after.getLocationCoordLat());
        appendDiff(diff, "/locationCoordLng", before.getLocationCoordLng(), after.getLocationCoordLng());
        appendDiff(diff, "/status", before.getStatus(), after.getStatus());
        appendDiff(diff, "/isCritical", before.getIsCritical(), after.getIsCritical());
        appendDiff(diff, "/memo", before.getMemo(), after.getMemo());
        appendDiff(diff, "/sortOrder", before.getSortOrder(), after.getSortOrder());
        appendDiff(diff, "/version", before.getVersion(), after.getVersion());
        return diff.toString();
    }

    private void appendDiff(List<String> diff, String path, Object before, Object after) {
        String beforeValue = String.valueOf(before);
        String afterValue = String.valueOf(after);
        if (!beforeValue.equals(afterValue)) {
            diff.add("{op=replace,path=" + path + ",from=" + beforeValue + ",value=" + afterValue + "}");
        }
    }

    private ScheduleItem copyItem(ScheduleItem source) {
        ScheduleItem copy = new ScheduleItem();
        copy.setScheduleItemId(source.getScheduleItemId());
        copy.setPatientJourneyId(source.getPatientJourneyId());
        copy.setItemType(source.getItemType());
        copy.setTitle(source.getTitle());
        copy.setScheduledStartAt(source.getScheduledStartAt());
        copy.setScheduledEndAt(source.getScheduledEndAt());
        copy.setLocationAddressEn(source.getLocationAddressEn());
        copy.setLocationCoordLat(source.getLocationCoordLat());
        copy.setLocationCoordLng(source.getLocationCoordLng());
        copy.setStatus(source.getStatus());
        copy.setIsCritical(source.getIsCritical());
        copy.setMemo(source.getMemo());
        copy.setSortOrder(source.getSortOrder());
        copy.setVersion(source.getVersion());
        return copy;
    }

    private static final class EditLock {
        private final String holderId;
        private final Object monitor = new Object();

        private EditLock(String holderId) {
            this.holderId = holderId;
        }
    }

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
        dto.setVersion(i.getVersion());
        return dto;
    }
}
