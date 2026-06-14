package com.kmedical.control;

import com.kmedical.adapter.PushAdapter;
import com.kmedical.domain.entity.PatientJourney;
import com.kmedical.domain.entity.ScheduleItem;
import com.kmedical.domain.enums.EditItineraryErrorCode;
import com.kmedical.domain.enums.EditItineraryState;
import com.kmedical.domain.enums.JourneyStatus;
import com.kmedical.domain.enums.ScheduleItemStatus;
import com.kmedical.domain.enums.UserRoleName;
import com.kmedical.dto.journey.EditItineraryRequestDTO;
import com.kmedical.dto.journey.EditItineraryResponseDTO;
import com.kmedical.dto.journey.PatientJourneyDTO;
import com.kmedical.dto.journey.ScheduleItemDTO;
import com.kmedical.dto.journey.ScheduleItemUpdateRequestDTO;
import com.kmedical.util.AuditLogger;
import com.kmedical.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SRV-C08 JourneyController.
 * Keeps journey and schedule operations while adding UC-ADM-07 dynamic flow.
 */
public class JourneyController {

    private static final int MAX_EXTERNAL_ATTEMPTS = 3;

    private final Map<String, PatientJourney> journeyStore = new ConcurrentHashMap<>();
    private final Map<String, List<ScheduleItem>> scheduleStore = new ConcurrentHashMap<>();
    private final Map<String, String> editLockHolders = new ConcurrentHashMap<>();
    private final Map<String, Object> editLockMonitors = new ConcurrentHashMap<>();
    private final Map<String, ScheduleItemDTO> delayedRealtimeDeltaStore = new ConcurrentHashMap<>();
    private final AlertController alertController;
    private final StaffAssignmentController staffAssignmentController;
    private final PushAdapter pushAdapter;

    public JourneyController(AlertController alertController) {
        this(alertController, null, null);
    }

    public JourneyController(AlertController alertController,
                             StaffAssignmentController staffAssignmentController,
                             PushAdapter pushAdapter) {
        this.alertController = alertController;
        this.staffAssignmentController = staffAssignmentController;
        this.pushAdapter = pushAdapter;
    }

    private void guardNotClosedDown() {
        if (SystemStateRegistry.getInstance().isClosedDown()) {
            AuditLogger.closedDownAccess("JourneyController", "UNKNOWN");
            throw new IllegalStateException("System is closed down. Customer operations are not permitted.");
        }
    }

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

    public PatientJourneyDTO getJourney(String journeyId) {
        guardNotClosedDown();
        ValidationUtil.requireNotBlank(journeyId, "journeyId");
        return toJourneyDTO(findJourney(journeyId));
    }

    public List<PatientJourneyDTO> getJourneysByPatient(String patientId) {
        guardNotClosedDown();
        ValidationUtil.requireNotBlank(patientId, "patientId");
        List<PatientJourneyDTO> result = new ArrayList<>();
        for (PatientJourney j : journeyStore.values()) {
            if (patientId.equals(j.getPatientId())) result.add(toJourneyDTO(j));
        }
        return result;
    }

    public ScheduleItemDTO updateScheduleItem(ScheduleItemUpdateRequestDTO request) {
        guardNotClosedDown();
        ValidationUtil.requireNotNull(request, "ScheduleItemUpdateRequestDTO");
        ValidationUtil.requireNotBlank(request.getScheduleItemId(), "scheduleItemId");

        ScheduleItem current = findScheduleItem(request.getScheduleItemId());
        EditItineraryRequestDTO editRequest = new EditItineraryRequestDTO(
                "LEGACY_ADMIN",
                UserRoleName.MANAGER,
                current.getPatientJourneyId(),
                request.getScheduleItemId(),
                versionOf(current),
                false,
                request.getItemType(),
                request.getTitle(),
                request.getScheduledStartAt(),
                request.getScheduledEndAt(),
                request.getLocationAddressEn(),
                request.getLocationCoordLat(),
                request.getLocationCoordLng(),
                request.getStatus(),
                request.getIsCritical(),
                request.getMemo(),
                request.getSortOrder(),
                null
        );
        EditItineraryResponseDTO response = editItinerary(editRequest);
        if (response.isSuccess()) return response.getUpdatedScheduleItem();
        if (response.getErrorCode() == EditItineraryErrorCode.VALIDATION_FAILED) {
            throw new IllegalArgumentException(response.getErrorDetail());
        }
        throw new IllegalStateException(response.getErrorDetail());
    }

    public EditItineraryResponseDTO editItinerary(EditItineraryRequestDTO request) {
        guardNotClosedDown();
        ValidationUtil.requireNotNull(request, "EditItineraryRequestDTO");

        EditItineraryState state = EditItineraryState.INIT;
        String operatorId = request.getOperatorId() != null ? request.getOperatorId() : "UNKNOWN";

        // L5 1.1 / A6: authenticate and authorize write access.
        if (!hasWritePermission(request.getOperatorRole())) {
            state = transition(state, "permissionDenied", EditItineraryState.REJECTED);
            AuditLogger.log("EDIT_ITINERARY_REJECTED", operatorId, request.getScheduleItemId(), false,
                    "reason=PERMISSION_DENIED");
            return EditItineraryResponseDTO.error(EditItineraryErrorCode.PERMISSION_DENIED,
                    "Permission denied for itinerary editing.", null, null, state);
        }
        state = transition(state, "authChecked", EditItineraryState.AUTH_CHECKED);

        // L5 2.1: operator selects target itinerary item.
        try {
            ValidationUtil.requireNotBlank(request.getScheduleItemId(), "scheduleItemId");
        } catch (IllegalArgumentException e) {
            return EditItineraryResponseDTO.error(EditItineraryErrorCode.VALIDATION_FAILED,
                    e.getMessage(), null, null, EditItineraryState.REJECTED);
        }
        ScheduleItem item = findScheduleItem(request.getScheduleItemId());
        state = transition(state, "selectItem", EditItineraryState.ITEM_SELECTED);

        // L5 3.1 / A7: acquire schedule-item edit lock.
        if (!acquireEditLock(request.getScheduleItemId(), operatorId)) {
            state = transition(state, "lockHeld", EditItineraryState.REJECTED);
            String lockHolderId = editLockHolders.get(request.getScheduleItemId());
            return EditItineraryResponseDTO.error(EditItineraryErrorCode.LOCK_HELD,
                    "Schedule item is already locked.", lockHolderId, null, state);
        }

        boolean lockOwned = true;
        try {
            Object monitor = editLockMonitors.get(request.getScheduleItemId());
            state = transition(state, "lockAcquired", EditItineraryState.LOCK_ACQUIRED);

            // L5 4.1: load current editable schedule snapshot.
            ScheduleItemDTO before = toItemDTO(item);
            state = transition(state, "editableFormReturned", EditItineraryState.EDIT_FORM_RETURNED);

            if (request.isCancelRequested()) {
                // L5 A5.1-A5.3: discard payload and return normal cancellation.
                state = transition(state, "cancel", EditItineraryState.CANCELLED);
                AuditLogger.log("EDIT_ITINERARY_CANCELLED", operatorId, request.getScheduleItemId(), true,
                        "payload discarded");
                return EditItineraryResponseDTO.cancelled(state);
            }

            // L5 5.1: submitted modification payload received.
            state = transition(state, "submit", EditItineraryState.SUBMITTED);

            ScheduleItemDTO after;
            List<String> assignedStaffIds;
            synchronized (monitor) {
                ScheduleItem lockedItem = findScheduleItem(request.getScheduleItemId());

                try {
                    // L5 6.1-6.3 / A1: validate schema, fields, and sibling time overlap.
                    validateSchedulePatch(request, lockedItem);
                } catch (IllegalArgumentException e) {
                    state = transition(state, "validationFailed", EditItineraryState.EDIT_FORM_RETURNED);
                    return EditItineraryResponseDTO.error(EditItineraryErrorCode.VALIDATION_FAILED,
                            e.getMessage(), null, toItemDTO(lockedItem), state);
                }
                state = transition(state, "validationOk", EditItineraryState.VALIDATED);

                // L5 7.1-7.2 / A2: optimistic version comparison.
                if (!request.getExpectedVersion().equals(versionOf(lockedItem))) {
                    state = transition(state, "versionConflict", EditItineraryState.REJECTED);
                    return EditItineraryResponseDTO.error(EditItineraryErrorCode.OPTIMISTIC_LOCK_CONFLICT,
                            "Expected version does not match latest schedule item version.",
                            null, toItemDTO(lockedItem), state);
                }

                // L5 8.1 and 9.1-9.2: apply entity changes, increment version, update staff association.
                applySchedulePatch(request, lockedItem);
                lockedItem.setVersion(versionOf(lockedItem) + 1);
                assignedStaffIds = updateAssignedStaffAssociation(request, lockedItem.getScheduleItemId(), operatorId);
                after = toItemDTO(lockedItem);
                state = transition(state, "persistOk", EditItineraryState.PERSISTED);
            }

            // L5 10.1: audit before/after diff after persistence and before external outputs.
            AuditLogger.log("EDIT_ITINERARY", operatorId, request.getScheduleItemId(), true,
                    "diff=" + computeDiff(before, after) + " assignedStaffIds=" + assignedStaffIds);
            state = transition(state, "auditLogged", EditItineraryState.AUDIT_LOGGED);

            // L5 11.1 and 12.3: start independent post-commit outputs concurrently.
            state = transition(state, "syncRequested", EditItineraryState.SYNC_REQUESTED);
            final boolean[] syncOkHolder = { true };
            Thread syncThread = new Thread(() -> syncOkHolder[0] = publishRealtimeSnapshotWithRetry(after));
            state = transition(state, "pushRequested", EditItineraryState.PUSH_REQUESTED);
            final boolean[] pushOkHolder = { true };
            String patientMagicLinkEndpoint = resolvePatientMagicLinkEndpoint(after);
            Thread pushThread = new Thread(() -> pushOkHolder[0] =
                    sendPushNotificationWithRetry(assignedStaffIds, patientMagicLinkEndpoint));
            syncThread.start();
            pushThread.start();
            joinPostCommitOutput(syncThread, "realtime sync");
            joinPostCommitOutput(pushThread, "push notification");

            boolean degradedSync = !syncOkHolder[0];
            if (degradedSync) {
                delayedRealtimeDeltaStore.put(after.getScheduleItemId(), after);
                AuditLogger.warn("EDIT_ITINERARY_SYNC_DEGRADED", "scheduleItemId=" + after.getScheduleItemId());
                state = transition(state, "syncFailedAfterRetry", EditItineraryState.SYNC_DEGRADED);
            }

            // L5 A4: push failure is recorded as warning, not a schedule rollback.
            boolean pushWarning = !pushOkHolder[0];
            if (pushWarning) {
                AuditLogger.warn("EDIT_ITINERARY_PUSH_FAILED", "scheduleItemId=" + after.getScheduleItemId());
            }

            state = transition(state, "returnUpdatedView", EditItineraryState.READY);
            return EditItineraryResponseDTO.success(after, assignedStaffIds, degradedSync, pushWarning, state);

        } catch (RuntimeException e) {
            state = transitionForFailure(state);
            AuditLogger.log("EDIT_ITINERARY", operatorId, request.getScheduleItemId(), false, e.getMessage());
            throw e;
        } finally {
            // L5 13.1: release edit lock for success, cancel, rejection, and exception paths.
            if (lockOwned) releaseEditLock(request.getScheduleItemId(), operatorId);
        }
    }

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

    public ScheduleItemDTO getScheduleItemForNavigation(String scheduleItemId) {
        guardNotClosedDown();
        ValidationUtil.requireNotBlank(scheduleItemId, "scheduleItemId");
        return toItemDTO(findScheduleItem(scheduleItemId));
    }

    private boolean hasWritePermission(UserRoleName role) {
        return role == UserRoleName.MASTER || role == UserRoleName.MANAGER;
    }

    private boolean acquireEditLock(String scheduleItemId, String operatorId) {
        editLockMonitors.computeIfAbsent(scheduleItemId, key -> new Object());
        return editLockHolders.putIfAbsent(scheduleItemId, operatorId) == null;
    }

    private void releaseEditLock(String scheduleItemId, String operatorId) {
        if (scheduleItemId == null) return;
        editLockHolders.remove(scheduleItemId, operatorId);
        if (!editLockHolders.containsKey(scheduleItemId)) {
            editLockMonitors.remove(scheduleItemId);
        }
    }

    private void validateSchedulePatch(EditItineraryRequestDTO request, ScheduleItem item) {
        if (request.getExpectedVersion() == null) {
            throw new IllegalArgumentException("expectedVersion is required.");
        }
        if (request.getPatientJourneyId() != null
                && !request.getPatientJourneyId().equals(item.getPatientJourneyId())) {
            throw new IllegalArgumentException("patientJourneyId does not match schedule item.");
        }
        if (request.getStatus() != null) validateStatusTransition(item.getStatus(), request.getStatus());
        if (request.getTitle() != null) ValidationUtil.requireLengthBetween(request.getTitle(), 1, 200, "title");
        if (request.getLocationCoordLat() != null) ValidationUtil.requireLatitude(request.getLocationCoordLat());
        if (request.getLocationCoordLng() != null) ValidationUtil.requireLongitude(request.getLocationCoordLng());
        if (request.getMemo() != null) ValidationUtil.requireMaxLength(request.getMemo(), 1000, "memo");
        if (request.getSortOrder() != null && request.getSortOrder() < 0) {
            throw new IllegalArgumentException("sortOrder must be >= 0.");
        }

        LocalDateTime proposedStart = request.getScheduledStartAt() != null
                ? request.getScheduledStartAt()
                : item.getScheduledStartAt();
        LocalDateTime proposedEnd = request.getScheduledEndAt() != null
                ? request.getScheduledEndAt()
                : item.getScheduledEndAt();
        if (proposedStart != null && proposedEnd != null) {
            ValidationUtil.requireEndAfterStart(proposedStart, proposedEnd);
            validateNoSiblingOverlap(item, proposedStart, proposedEnd);
        }
    }

    private void validateNoSiblingOverlap(ScheduleItem target, LocalDateTime proposedStart, LocalDateTime proposedEnd) {
        for (ScheduleItem sibling : scheduleStore.getOrDefault(target.getPatientJourneyId(), Collections.emptyList())) {
            if (target.getScheduleItemId().equals(sibling.getScheduleItemId())) continue;
            if (sibling.getScheduledStartAt() == null || sibling.getScheduledEndAt() == null) continue;
            boolean overlaps = proposedStart.isBefore(sibling.getScheduledEndAt())
                    && sibling.getScheduledStartAt().isBefore(proposedEnd);
            if (overlaps) {
                throw new IllegalArgumentException("Schedule time overlaps sibling item: "
                        + sibling.getScheduleItemId());
            }
        }
    }

    private void applySchedulePatch(EditItineraryRequestDTO request, ScheduleItem item) {
        if (request.getItemType() != null) item.setItemType(request.getItemType());
        if (request.getStatus() != null) item.setStatus(request.getStatus());
        if (request.getTitle() != null) item.setTitle(request.getTitle());
        if (request.getScheduledStartAt() != null) item.setScheduledStartAt(request.getScheduledStartAt());
        if (request.getScheduledEndAt() != null) item.setScheduledEndAt(request.getScheduledEndAt());
        if (request.getLocationAddressEn() != null) item.setLocationAddressEn(request.getLocationAddressEn());
        if (request.getLocationCoordLat() != null) item.setLocationCoordLat(request.getLocationCoordLat());
        if (request.getLocationCoordLng() != null) item.setLocationCoordLng(request.getLocationCoordLng());
        if (request.getIsCritical() != null) item.setIsCritical(request.getIsCritical());
        if (request.getMemo() != null) item.setMemo(request.getMemo());
        if (request.getSortOrder() != null) item.setSortOrder(request.getSortOrder());
    }

    private List<String> updateAssignedStaffAssociation(EditItineraryRequestDTO request,
                                                        String scheduleItemId,
                                                        String operatorId) {
        if (staffAssignmentController == null) {
            return request.getAssignedStaffIds() == null
                    ? Collections.emptyList()
                    : new ArrayList<>(request.getAssignedStaffIds());
        }
        if (request.getAssignedStaffIds() == null) {
            return staffAssignmentController.getAssignedStaffIds(scheduleItemId);
        }
        return staffAssignmentController.replaceAssignedStaffIds(
                scheduleItemId,
                request.getAssignedStaffIds(),
                operatorId
        );
    }

    private boolean publishRealtimeSnapshotWithRetry(ScheduleItemDTO snapshot) {
        for (int attempt = 1; attempt <= MAX_EXTERNAL_ATTEMPTS; attempt++) {
            if (snapshot != null) return true;
        }
        return false;
    }

    private boolean sendPushNotificationWithRetry(List<String> staffIds, String patientMagicLinkEndpoint) {
        if (pushAdapter == null) return true;
        List<String> recipients = new ArrayList<>();
        if (staffIds != null) recipients.addAll(staffIds);
        if (patientMagicLinkEndpoint != null && !patientMagicLinkEndpoint.isBlank()) {
            recipients.add(patientMagicLinkEndpoint);
        }
        if (recipients.isEmpty()) return true;

        for (int attempt = 1; attempt <= MAX_EXTERNAL_ATTEMPTS; attempt++) {
            boolean allSent = true;
            for (String recipient : recipients) {
                allSent &= pushAdapter.sendPush(
                        recipient,
                        "Schedule Updated",
                        "An itinerary schedule item has been updated."
                );
            }
            if (allSent) return true;
        }
        return false;
    }

    private void joinPostCommitOutput(Thread thread, String outputName) {
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            AuditLogger.warn("EDIT_ITINERARY_OUTPUT_INTERRUPTED", outputName);
        }
    }

    private String resolvePatientMagicLinkEndpoint(ScheduleItemDTO item) {
        return item.getPatientJourneyId() != null ? "patientJourney:" + item.getPatientJourneyId() : null;
    }

    private String computeDiff(ScheduleItemDTO before, ScheduleItemDTO after) {
        List<String> patches = new ArrayList<>();
        addPatch(patches, "/itemType", enumName(before.getItemType()), enumName(after.getItemType()));
        addPatch(patches, "/title", before.getTitle(), after.getTitle());
        addPatch(patches, "/scheduledStartAt", stringValue(before.getScheduledStartAt()), stringValue(after.getScheduledStartAt()));
        addPatch(patches, "/scheduledEndAt", stringValue(before.getScheduledEndAt()), stringValue(after.getScheduledEndAt()));
        addPatch(patches, "/locationAddressEn", before.getLocationAddressEn(), after.getLocationAddressEn());
        addPatch(patches, "/locationCoordLat", stringValue(before.getLocationCoordLat()), stringValue(after.getLocationCoordLat()));
        addPatch(patches, "/locationCoordLng", stringValue(before.getLocationCoordLng()), stringValue(after.getLocationCoordLng()));
        addPatch(patches, "/status", enumName(before.getStatus()), enumName(after.getStatus()));
        addPatch(patches, "/isCritical", stringValue(before.getIsCritical()), stringValue(after.getIsCritical()));
        addPatch(patches, "/memo", before.getMemo(), after.getMemo());
        addPatch(patches, "/sortOrder", stringValue(before.getSortOrder()), stringValue(after.getSortOrder()));
        addPatch(patches, "/version", stringValue(before.getVersion()), stringValue(after.getVersion()));
        return patches.toString();
    }

    private void addPatch(List<String> patches, String path, String before, String after) {
        if (before == null ? after != null : !before.equals(after)) {
            patches.add("{op=replace,path=" + path + ",from=" + before + ",value=" + after + "}");
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private String enumName(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private EditItineraryState transition(EditItineraryState current, String event, EditItineraryState next) {
        boolean allowed =
                current == EditItineraryState.INIT && (next == EditItineraryState.AUTH_CHECKED || next == EditItineraryState.REJECTED)
                || current == EditItineraryState.AUTH_CHECKED && next == EditItineraryState.ITEM_SELECTED
                || current == EditItineraryState.ITEM_SELECTED && (next == EditItineraryState.LOCK_ACQUIRED || next == EditItineraryState.REJECTED)
                || current == EditItineraryState.LOCK_ACQUIRED && next == EditItineraryState.EDIT_FORM_RETURNED
                || current == EditItineraryState.EDIT_FORM_RETURNED && (next == EditItineraryState.SUBMITTED || next == EditItineraryState.CANCELLED)
                || current == EditItineraryState.SUBMITTED && (next == EditItineraryState.VALIDATED || next == EditItineraryState.EDIT_FORM_RETURNED)
                || current == EditItineraryState.VALIDATED && (next == EditItineraryState.PERSISTED || next == EditItineraryState.REJECTED)
                || current == EditItineraryState.PERSISTED && next == EditItineraryState.AUDIT_LOGGED
                || current == EditItineraryState.AUDIT_LOGGED && next == EditItineraryState.SYNC_REQUESTED
                || current == EditItineraryState.SYNC_REQUESTED && (next == EditItineraryState.SYNC_DEGRADED || next == EditItineraryState.PUSH_REQUESTED)
                || current == EditItineraryState.SYNC_DEGRADED && (next == EditItineraryState.PUSH_REQUESTED || next == EditItineraryState.READY)
                || current == EditItineraryState.PUSH_REQUESTED && (next == EditItineraryState.SYNC_DEGRADED || next == EditItineraryState.READY);
        if (!allowed) {
            throw new IllegalStateException("Invalid edit itinerary transition: "
                    + current + " --" + event + "--> " + next);
        }
        return next;
    }

    private EditItineraryState transitionForFailure(EditItineraryState current) {
        if (current == EditItineraryState.READY
                || current == EditItineraryState.CANCELLED
                || current == EditItineraryState.REJECTED) {
            return current;
        }
        return EditItineraryState.FAILED;
    }

    private void validateStatusTransition(ScheduleItemStatus current, ScheduleItemStatus next) {
        if (current == ScheduleItemStatus.SCHEDULED && next == ScheduleItemStatus.IN_PROGRESS) return;
        if (current == ScheduleItemStatus.IN_PROGRESS && next == ScheduleItemStatus.COMPLETED) return;
        if (current == next) return;
        throw new IllegalStateException(
                "Invalid status transition: " + current + " -> " + next
                        + ". Allowed: SCHEDULED->IN_PROGRESS->COMPLETED.");
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

    private Integer versionOf(ScheduleItem item) {
        return item.getVersion() != null ? item.getVersion() : 0;
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
        dto.setVersion(versionOf(i));
        return dto;
    }
}
