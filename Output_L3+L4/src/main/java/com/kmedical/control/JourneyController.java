package com.kmedical.control;

import com.kmedical.adapter.PushAdapter;
import com.kmedical.adapter.RealtimeSyncAdapter;
import com.kmedical.domain.entity.PatientJourney;
import com.kmedical.domain.entity.ScheduleItem;
import com.kmedical.domain.enums.EditItineraryErrorCode;
import com.kmedical.domain.enums.EditItineraryStatus;
import com.kmedical.domain.enums.JourneyStatus;
import com.kmedical.domain.enums.ScheduleItemStatus;
import com.kmedical.dto.journey.EditItineraryResponseDTO;
import com.kmedical.dto.journey.PatientJourneyDTO;
import com.kmedical.dto.journey.ScheduleItemDTO;
import com.kmedical.dto.journey.ScheduleItemUpdateRequestDTO;
import com.kmedical.util.AuditLogger;
import com.kmedical.util.ValidationUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SRV-C08 — JourneyController
 * 책임: 여정 생성, 일정 관리, 상태 동기화, 좌표 제공.
 * UC: UC-A06, UC-P08, UC-P10, UC-P11, UC-S04, UC-S07
 * 제약: ScheduleItem 상태 전이 SCHEDULED→IN_PROGRESS→COMPLETED 순서만 허용
 * NFR 적용: ConcurrentHashMap, CopyOnWriteArrayList, 좌표 범위 검증, scheduledEndAt > scheduledStartAt
 */
public class JourneyController {

    private static final long RETRY_INITIAL_DELAY_MILLIS = 1000L;
    private static final long RETRY_BACKOFF_MULTIPLIER = 2L;
    private static final int RETRY_MAX_ATTEMPTS = 3;

    private final Map<String, PatientJourney> journeyStore = new ConcurrentHashMap<>();
    private final Map<String, List<ScheduleItem>> scheduleStore = new ConcurrentHashMap<>();
    private final Map<String, String> editLocks = new ConcurrentHashMap<>();
    private final Map<String, Object> scheduleItemMutexes = new ConcurrentHashMap<>();
    private final AlertController alertController;
    private final PushAdapter pushAdapter;
    private final RealtimeSyncAdapter realtimeSyncAdapter;

    public JourneyController(AlertController alertController) {
        this(alertController, new PushAdapter() {
            public boolean sendPush(String recipientUserId, String title, String body) {
                return true;
            }

            public void sendBulkPush(List<String> recipientUserIds, String title, String body) {
                // no-op compatibility adapter
            }
        }, snapshot -> true);
    }

    public JourneyController(AlertController alertController,
                             PushAdapter pushAdapter,
                             RealtimeSyncAdapter realtimeSyncAdapter) {
        this.alertController = alertController;
        this.pushAdapter = pushAdapter;
        this.realtimeSyncAdapter = realtimeSyncAdapter;
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
        ValidationUtil.requireNotNull(request, "ScheduleItemUpdateRequestDTO");
        if (request.getExpectedVersion() == null && request.getScheduleItemId() != null) {
            request.setExpectedVersion(findScheduleItem(request.getScheduleItemId()).getVersion());
        }
        EditItineraryResponseDTO response = editItinerary(request);
        if (response.isSuccess()) return response.getUpdatedScheduleItem();
        if (response.isCancelled()) return null;
        if (response.getErrorCode() == EditItineraryErrorCode.VALIDATION_FAILED) {
            throw new IllegalArgumentException(response.getFieldErrors().toString());
        }
        throw new IllegalStateException(response.getErrorCode() + ": " + response.getErrorDetail());
    }

    /**
     * UC-ADM-07 Edit Itinerary Aggregate 흐름.
     */
    public EditItineraryResponseDTO editItinerary(ScheduleItemUpdateRequestDTO request) {
        guardNotClosedDown();
        ValidationUtil.requireNotNull(request, "ScheduleItemUpdateRequestDTO");

        EditItinerarySession session = new EditItinerarySession();
        String scheduleItemId = request.getScheduleItemId();
        String operatorId = request.getOperatorId();

        // L5 1.1 / A6: Authenticate/RBAC 결과 확인. 현재 L4는 RBAC 권한 조회 API가 없어 operatorId 존재를 쓰기 권한 게이트로 사용한다.
        if (operatorId == null || operatorId.isBlank()) {
            AuditLogger.log("EDIT_ITINERARY_REJECTED", operatorId, scheduleItemId, false,
                    "errorCode=PERMISSION_DENIED reason=missing operatorId");
            session.transition(EditItineraryStatus.REJECTED, "permissionDenied");
            return EditItineraryResponseDTO.error(
                    EditItineraryErrorCode.PERMISSION_DENIED,
                    "operatorId is required for itinerary editing.",
                    session.getStatus());
        }
        session.transition(EditItineraryStatus.AUTH_CHECKED, "authenticateOk");

        ValidationUtil.requireNotBlank(scheduleItemId, "scheduleItemId");

        // L5 2.1~2.3: 대상 일정 항목 선택.
        ScheduleItem selected = findScheduleItem(scheduleItemId);
        session.transition(EditItineraryStatus.ITEM_SELECTED, "selectItem");

        // L5 3.1 / A7: scheduleItemId 단위 편집 락 획득.
        String lockHolder = acquireEditLock(scheduleItemId, operatorId);
        if (lockHolder != null) {
            session.transition(EditItineraryStatus.REJECTED, "lockHeld");
            return EditItineraryResponseDTO.error(
                    EditItineraryErrorCode.LOCK_HELD,
                    lockHolder,
                    session.getStatus());
        }

        boolean lockOwned = true;
        ScheduleItemDTO before;
        ScheduleItemDTO after;
        List<String> assignedStaffIds;

        try {
            session.transition(EditItineraryStatus.LOCK_ACQUIRED, "lockAcquired");
            Object mutex = scheduleItemMutexes.computeIfAbsent(scheduleItemId, k -> new Object());
            synchronized (mutex) {
                ScheduleItem item = findScheduleItem(scheduleItemId);

                // L5 4.1~4.2: 현재 일정과 편집 가능 폼 반환.
                before = toItemDTO(item);
                session.transition(EditItineraryStatus.EDIT_FORM_RETURNED, "returnEditForm");

                // L5 A5.1~A5.3: 저장 전 사용자 취소는 정상 취소 응답으로 종료한다.
                if (Boolean.TRUE.equals(request.getCancelRequested())) {
                    session.transition(EditItineraryStatus.CANCELLED, "cancel");
                    AuditLogger.log("EDIT_ITINERARY_CANCELLED", operatorId, scheduleItemId, true,
                            "payload discarded");
                    return EditItineraryResponseDTO.cancelled(session.getStatus());
                }

                // L5 5.1~6.3 / A1: 제출 payload 검증. 검증 전에는 Entity를 변경하지 않는다.
                session.transition(EditItineraryStatus.SUBMITTED, "submitPayload");
                Map<String, String> fieldErrors = validateEditPayload(request, item);
                if (!fieldErrors.isEmpty()) {
                    session.transition(EditItineraryStatus.EDIT_FORM_RETURNED, "validationFailed");
                    return EditItineraryResponseDTO.validationError(fieldErrors, session.getStatus());
                }
                session.transition(EditItineraryStatus.VALIDATED, "validationOk");

                // L5 7.1~7.2 / A2: expectedVersion 기반 낙관적 버전 비교.
                int expectedVersion = request.getExpectedVersion();
                int actualVersion = item.getVersion();
                if (expectedVersion != actualVersion) {
                    ScheduleItemDTO latest = toItemDTO(item);
                    ScheduleItemDTO submitted = toSubmittedDTO(before, request);
                    session.transition(EditItineraryStatus.REJECTED, "versionConflict");
                    return EditItineraryResponseDTO.conflict(
                            latest,
                            submitted,
                            "expectedVersion=" + expectedVersion + " actualVersion=" + actualVersion,
                            session.getStatus());
                }

                // L5 8.1~9.2: 변경 적용, version +1, 배정 실무자 연관 갱신을 하나의 임계영역에서 처리한다.
                applyScheduleChanges(item, request);
                item.setVersion(item.getVersion() + 1);
                assignedStaffIds = new ArrayList<>(item.getAssignedStaffIds());
                after = toItemDTO(item);
                session.transition(EditItineraryStatus.PERSISTED, "persistOk");
            }

            // L5 10.1: 변경 전후 JSON Patch 형태 diff 감사 로그.
            AuditLogger.log("EDIT_ITINERARY_UPDATED", operatorId, scheduleItemId, true,
                    "diff=" + computeJsonPatchDiff(before, after));
            session.transition(EditItineraryStatus.AUDIT_LOGGED, "auditLogged");

            // L5 11.1 / 12.3 concurrent: sync와 push는 저장 임계영역 밖에서 독립 출력으로 실행한다. 별도 ACK는 구현하지 않는다.
            session.transition(EditItineraryStatus.SYNC_REQUESTED, "syncPublish");
            String patientEndpoint = resolvePatientMagicLinkEndpoint(request, after);
            CompletableFuture<Boolean> syncFuture =
                    CompletableFuture.supplyAsync(() -> publishRealtimeWithRetry(after));
            CompletableFuture<Boolean> pushFuture =
                    CompletableFuture.supplyAsync(() -> queuePushWithRetry(assignedStaffIds, patientEndpoint, after));

            boolean syncOk = syncFuture.join();
            boolean pushOk = pushFuture.join();

            boolean degradedSync = false;
            if (syncOk) {
                session.transition(EditItineraryStatus.PUSH_REQUESTED, "syncOk");
            } else {
                // L5 11.A3.1~11.A3.3: 지연 delta 기록은 현재 L4 저장소가 없어 감사 경고 로그로 남기고 degraded success로 계속한다.
                AuditLogger.warn("EDIT_ITINERARY_SYNC_DEGRADED",
                        "scheduleItemId=" + scheduleItemId + " delta=" + computeJsonPatchDiff(before, after));
                degradedSync = true;
                session.transition(EditItineraryStatus.SYNC_DEGRADED, "syncFailedAfterRetry");
                session.transition(EditItineraryStatus.PUSH_REQUESTED, "continuePush");
            }

            boolean pushWarning = false;
            if (!pushOk) {
                // L5 12.A4.1~12.A4.2: 푸시 실패는 본문 변경을 롤백하지 않고 경고 로그로 기록한다.
                AuditLogger.warn("EDIT_ITINERARY_PUSH_FAILED",
                        "scheduleItemId=" + scheduleItemId + " recipients=" + assignedStaffIds);
                pushWarning = true;
            }
            session.transition(EditItineraryStatus.READY, pushOk ? "pushQueued" : "pushFailedAfterRetry");

            // L5 14.1~15.1: 갱신 일정 뷰 반환 및 dashboard ready 상태 복귀.
            return EditItineraryResponseDTO.success(after, assignedStaffIds, degradedSync, pushWarning, session.getStatus());
        } finally {
            if (lockOwned) {
                // L5 13.1 / A5.2: 성공, 거절, 취소, 예외 상황 모두 finally에서 편집 락을 해제한다.
                releaseEditLock(scheduleItemId, operatorId);
            }
        }
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
        item.setAssignedStaffIds(dto.getAssignedStaffIds());

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

    // Private helpers

    private String acquireEditLock(String scheduleItemId, String operatorId) {
        return editLocks.putIfAbsent(scheduleItemId, operatorId);
    }

    private void releaseEditLock(String scheduleItemId, String operatorId) {
        editLocks.remove(scheduleItemId, operatorId);
    }

    private Map<String, String> validateEditPayload(ScheduleItemUpdateRequestDTO request, ScheduleItem item) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (request.getExpectedVersion() == null) {
            errors.put("expectedVersion", "expectedVersion is required.");
        }
        if (request.getPatientJourneyId() != null
                && !request.getPatientJourneyId().equals(item.getPatientJourneyId())) {
            errors.put("patientJourneyId", "patientJourneyId does not match the selected schedule item.");
        }
        if (request.getTitle() != null
                && (request.getTitle().isBlank() || request.getTitle().length() > 200)) {
            errors.put("title", "title length must be between 1 and 200.");
        }
        if (request.getMemo() != null && request.getMemo().length() > 1000) {
            errors.put("memo", "memo length must be <= 1000.");
        }
        if (request.getSortOrder() != null && request.getSortOrder() < 0) {
            errors.put("sortOrder", "sortOrder must be >= 0.");
        }
        if (request.getLocationCoordLat() != null && !isBetween(request.getLocationCoordLat(), "-90", "90")) {
            errors.put("locationCoordLat", "latitude must be between -90 and 90.");
        }
        if (request.getLocationCoordLng() != null && !isBetween(request.getLocationCoordLng(), "-180", "180")) {
            errors.put("locationCoordLng", "longitude must be between -180 and 180.");
        }

        LocalDateTime nextStart = request.getScheduledStartAt() != null
                ? request.getScheduledStartAt()
                : item.getScheduledStartAt();
        LocalDateTime nextEnd = request.getScheduledEndAt() != null
                ? request.getScheduledEndAt()
                : item.getScheduledEndAt();
        if (nextStart != null && nextEnd != null && !nextEnd.isAfter(nextStart)) {
            errors.put("scheduledEndAt", "scheduledEndAt must be after scheduledStartAt.");
        }
        if (request.getStatus() != null) {
            String statusError = validateStatusTransitionMessage(item.getStatus(), request.getStatus());
            if (statusError != null) errors.put("status", statusError);
        }
        if (nextStart != null && nextEnd != null && hasSiblingTimeOverlap(item, nextStart, nextEnd)) {
            errors.put("scheduledStartAt", "schedule overlaps with a sibling item.");
        }
        return errors;
    }

    private boolean isBetween(BigDecimal value, String min, String max) {
        return value.compareTo(new BigDecimal(min)) >= 0 && value.compareTo(new BigDecimal(max)) <= 0;
    }

    private boolean hasSiblingTimeOverlap(ScheduleItem item, LocalDateTime start, LocalDateTime end) {
        for (ScheduleItem sibling : scheduleStore.getOrDefault(item.getPatientJourneyId(), new ArrayList<>())) {
            if (item.getScheduleItemId().equals(sibling.getScheduleItemId())) continue;
            if (sibling.getScheduledStartAt() == null || sibling.getScheduledEndAt() == null) continue;
            if (start.isBefore(sibling.getScheduledEndAt()) && end.isAfter(sibling.getScheduledStartAt())) {
                return true;
            }
        }
        return false;
    }

    private void applyScheduleChanges(ScheduleItem item, ScheduleItemUpdateRequestDTO request) {
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
        if (!request.getAssignedStaffIds().isEmpty()) item.setAssignedStaffIds(request.getAssignedStaffIds());
    }

    private boolean publishRealtimeWithRetry(ScheduleItemDTO snapshot) {
        long delayMillis = RETRY_INITIAL_DELAY_MILLIS;
        for (int attempt = 1; attempt <= RETRY_MAX_ATTEMPTS; attempt++) {
            if (realtimeSyncAdapter.publishScheduleSnapshot(snapshot)) return true;
            if (attempt < RETRY_MAX_ATTEMPTS) {
                sleep(delayMillis);
                delayMillis *= RETRY_BACKOFF_MULTIPLIER;
            }
        }
        return false;
    }

    private boolean queuePushWithRetry(List<String> assignedStaffIds,
                                       String patientMagicLinkEndpoint,
                                       ScheduleItemDTO snapshot) {
        List<String> recipients = new ArrayList<>(assignedStaffIds);
        if (patientMagicLinkEndpoint != null && !patientMagicLinkEndpoint.isBlank()) {
            recipients.add(patientMagicLinkEndpoint);
        }
        if (recipients.isEmpty()) return true;

        long delayMillis = RETRY_INITIAL_DELAY_MILLIS;
        for (int attempt = 1; attempt <= RETRY_MAX_ATTEMPTS; attempt++) {
            boolean allSent = true;
            for (String recipient : recipients) {
                boolean sent = pushAdapter.sendPush(
                        recipient,
                        "Schedule Updated",
                        "Schedule item updated: " + snapshot.getScheduleItemId());
                allSent = allSent && sent;
            }
            if (allSent) return true;
            if (attempt < RETRY_MAX_ATTEMPTS) {
                sleep(delayMillis);
                delayMillis *= RETRY_BACKOFF_MULTIPLIER;
            }
        }
        return false;
    }

    private void sleep(long delayMillis) {
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String resolvePatientMagicLinkEndpoint(ScheduleItemUpdateRequestDTO request, ScheduleItemDTO updated) {
        if (request.getPatientMagicLinkEndpoint() != null && !request.getPatientMagicLinkEndpoint().isBlank()) {
            return request.getPatientMagicLinkEndpoint();
        }
        return "patient-journey:" + updated.getPatientJourneyId();
    }

    private String validateStatusTransitionMessage(ScheduleItemStatus current, ScheduleItemStatus next) {
        if (current == ScheduleItemStatus.SCHEDULED && next == ScheduleItemStatus.IN_PROGRESS) return null;
        if (current == ScheduleItemStatus.IN_PROGRESS && next == ScheduleItemStatus.COMPLETED) return null;
        if (current == next) return null;
        return "Invalid status transition: " + current + " -> " + next
                + ". Allowed: SCHEDULED -> IN_PROGRESS -> COMPLETED.";
    }

    private void validateStatusTransition(ScheduleItemStatus current, ScheduleItemStatus next) {
        String message = validateStatusTransitionMessage(current, next);
        if (message != null) throw new IllegalStateException(message);
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
        dto.setAssignedStaffIds(i.getAssignedStaffIds());
        return dto;
    }

    private ScheduleItemDTO toSubmittedDTO(ScheduleItemDTO before, ScheduleItemUpdateRequestDTO request) {
        ScheduleItemDTO dto = copyDTO(before);
        if (request.getItemType() != null) dto.setItemType(request.getItemType());
        if (request.getTitle() != null) dto.setTitle(request.getTitle());
        if (request.getScheduledStartAt() != null) dto.setScheduledStartAt(request.getScheduledStartAt());
        if (request.getScheduledEndAt() != null) dto.setScheduledEndAt(request.getScheduledEndAt());
        if (request.getLocationAddressEn() != null) dto.setLocationAddressEn(request.getLocationAddressEn());
        if (request.getLocationCoordLat() != null) dto.setLocationCoordLat(request.getLocationCoordLat());
        if (request.getLocationCoordLng() != null) dto.setLocationCoordLng(request.getLocationCoordLng());
        if (request.getStatus() != null) dto.setStatus(request.getStatus());
        if (request.getIsCritical() != null) dto.setIsCritical(request.getIsCritical());
        if (request.getMemo() != null) dto.setMemo(request.getMemo());
        if (request.getSortOrder() != null) dto.setSortOrder(request.getSortOrder());
        if (!request.getAssignedStaffIds().isEmpty()) dto.setAssignedStaffIds(request.getAssignedStaffIds());
        return dto;
    }

    private ScheduleItemDTO copyDTO(ScheduleItemDTO source) {
        ScheduleItemDTO dto = new ScheduleItemDTO();
        dto.setScheduleItemId(source.getScheduleItemId());
        dto.setPatientJourneyId(source.getPatientJourneyId());
        dto.setItemType(source.getItemType());
        dto.setTitle(source.getTitle());
        dto.setScheduledStartAt(source.getScheduledStartAt());
        dto.setScheduledEndAt(source.getScheduledEndAt());
        dto.setLocationAddressEn(source.getLocationAddressEn());
        dto.setLocationCoordLat(source.getLocationCoordLat());
        dto.setLocationCoordLng(source.getLocationCoordLng());
        dto.setStatus(source.getStatus());
        dto.setIsCritical(source.getIsCritical());
        dto.setMemo(source.getMemo());
        dto.setSortOrder(source.getSortOrder());
        dto.setVersion(source.getVersion());
        dto.setAssignedStaffIds(source.getAssignedStaffIds());
        return dto;
    }

    private List<String> computeJsonPatchDiff(ScheduleItemDTO before, ScheduleItemDTO after) {
        List<String> diff = new ArrayList<>();
        addDiff(diff, "/itemType", before.getItemType(), after.getItemType());
        addDiff(diff, "/title", before.getTitle(), after.getTitle());
        addDiff(diff, "/scheduledStartAt", before.getScheduledStartAt(), after.getScheduledStartAt());
        addDiff(diff, "/scheduledEndAt", before.getScheduledEndAt(), after.getScheduledEndAt());
        addDiff(diff, "/locationAddressEn", before.getLocationAddressEn(), after.getLocationAddressEn());
        addDiff(diff, "/locationCoordLat", before.getLocationCoordLat(), after.getLocationCoordLat());
        addDiff(diff, "/locationCoordLng", before.getLocationCoordLng(), after.getLocationCoordLng());
        addDiff(diff, "/status", before.getStatus(), after.getStatus());
        addDiff(diff, "/isCritical", before.getIsCritical(), after.getIsCritical());
        addDiff(diff, "/memo", before.getMemo(), after.getMemo());
        addDiff(diff, "/sortOrder", before.getSortOrder(), after.getSortOrder());
        addDiff(diff, "/version", before.getVersion(), after.getVersion());
        addDiff(diff, "/assignedStaffIds", before.getAssignedStaffIds(), after.getAssignedStaffIds());
        return diff;
    }

    private void addDiff(List<String> diff, String path, Object before, Object after) {
        if (!Objects.equals(before, after)) {
            diff.add("{op=replace,path=" + path + ",value=" + after + "}");
        }
    }

    private static final class EditItinerarySession {
        private EditItineraryStatus status = EditItineraryStatus.INIT;

        EditItineraryStatus getStatus() {
            return status;
        }

        void transition(EditItineraryStatus next, String event) {
            if (!isAllowed(status, next)) {
                throw new IllegalStateException(
                        "Invalid UC-ADM-07 transition: " + status + " --" + event + "--> " + next);
            }
            status = next;
        }

        private boolean isAllowed(EditItineraryStatus current, EditItineraryStatus next) {
            if (current == EditItineraryStatus.INIT
                    && (next == EditItineraryStatus.AUTH_CHECKED || next == EditItineraryStatus.REJECTED)) return true;
            if (current == EditItineraryStatus.AUTH_CHECKED && next == EditItineraryStatus.ITEM_SELECTED) return true;
            if (current == EditItineraryStatus.ITEM_SELECTED
                    && (next == EditItineraryStatus.LOCK_ACQUIRED || next == EditItineraryStatus.REJECTED)) return true;
            if (current == EditItineraryStatus.LOCK_ACQUIRED && next == EditItineraryStatus.EDIT_FORM_RETURNED) return true;
            if (current == EditItineraryStatus.EDIT_FORM_RETURNED
                    && (next == EditItineraryStatus.SUBMITTED || next == EditItineraryStatus.CANCELLED)) return true;
            if (current == EditItineraryStatus.SUBMITTED
                    && (next == EditItineraryStatus.VALIDATED || next == EditItineraryStatus.EDIT_FORM_RETURNED)) return true;
            if (current == EditItineraryStatus.VALIDATED
                    && (next == EditItineraryStatus.PERSISTED || next == EditItineraryStatus.REJECTED)) return true;
            if (current == EditItineraryStatus.PERSISTED && next == EditItineraryStatus.AUDIT_LOGGED) return true;
            if (current == EditItineraryStatus.AUDIT_LOGGED && next == EditItineraryStatus.SYNC_REQUESTED) return true;
            if (current == EditItineraryStatus.SYNC_REQUESTED
                    && (next == EditItineraryStatus.PUSH_REQUESTED || next == EditItineraryStatus.SYNC_DEGRADED)) return true;
            if (current == EditItineraryStatus.SYNC_DEGRADED && next == EditItineraryStatus.PUSH_REQUESTED) return true;
            if (current == EditItineraryStatus.PUSH_REQUESTED && next == EditItineraryStatus.READY) return true;
            return next == EditItineraryStatus.FAILED;
        }
    }
}
