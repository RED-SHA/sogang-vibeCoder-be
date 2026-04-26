// Use Case: UC-ADM-07 Edit Itinerary
// Source: UseCaseDescription_UC-ADM-07_reviewd.md
//   - Description Step 1..15
//   - Alternatives A1..A7
//   - 부록 §1 (retry), §2 (diff), §3 (dashboard ready), §4 (trace)
// Implementation guideline:
//   - Each Description step → method call in order
//   - Included Abstract UC → independent class (AuthenticateUser, AuditLogAction,
//                                                SynchronizeRealtimeState, SendPushNotification)
//   - Step number annotated next to each line
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class EditItinerary {

    private final AuthenticateUser authenticateUser;                   // Dependency
    private final AuditLogAction auditLogAction;                       // Dependency
    private final SynchronizeRealtimeState synchronizeRealtimeState;   // Dependency
    private final SendPushNotification sendPushNotification;           // Dependency
    private final ItineraryRepository itineraryRepository;             // 일정 저장소
    private final DelayedDispatchQueue delayedDispatchQueue;           // 지연 발송 큐 (A3.1)
    private final AlertCenter alertCenter;                             // 알림 센터 (A4.1)

    public EditItinerary(AuthenticateUser authenticateUser,
                         AuditLogAction auditLogAction,
                         SynchronizeRealtimeState synchronizeRealtimeState,
                         SendPushNotification sendPushNotification,
                         ItineraryRepository itineraryRepository,
                         DelayedDispatchQueue delayedDispatchQueue,
                         AlertCenter alertCenter) {
        this.authenticateUser = authenticateUser;
        this.auditLogAction = auditLogAction;
        this.synchronizeRealtimeState = synchronizeRealtimeState;
        this.sendPushNotification = sendPushNotification;
        this.itineraryRepository = itineraryRepository;
        this.delayedDispatchQueue = delayedDispatchQueue;
        this.alertCenter = alertCenter;
    }

    // Main scenario entry
    public EditItineraryResponse execute(AgencyOperator operator) {

        // ----- Step 1: Authenticate User result check (also gate A6 RBAC) -----
        AuthenticateUser.AuthenticationResult auth =
                authenticateUser.run(operator.getOperatorId());                                    // Step 1

        if (!auth.isWritePermissionGranted()) {                                                    // Step 1 / A6
            // A6.1: block action
            // A6.2: include Audit Log Action with operator_id, rejection reason
            auditLogAction.runRejection(operator.getOperatorId(),
                                        auth.getDenialReason(),
                                        Instant.now());                                            // A6.2
            // A6.3: return permission error code
            // A6.4: end use case
            return EditItineraryResponse.error(ErrorCode.PERMISSION_DENIED, auth.getDenialReason()); // A6.3 / A6.4
        }

        // ----- Step 2: operator selects target itinerary item from case dashboard -----
        String selectedItemId = waitForOperatorSelection(operator);                                // Step 2

        // ----- Step 3: acquire edit lock (A7 on failure) -----
        try {
            itineraryRepository.acquireLock(selectedItemId);                                       // Step 3
        } catch (LockHeldException lhe) {
            // A7: return lock_held error with lock holder id, end use case
            return EditItineraryResponse.error(ErrorCode.LOCK_HELD, lhe.getLockHolderId());        // Step 3 / A7
        }

        return runLockedFlow(operator, selectedItemId);
    }

    private EditItineraryResponse runLockedFlow(AgencyOperator operator, String selectedItemId) {

        // Loop label for A2 (return to Step 4 after merge view)
        ItineraryItem updated = null;
        boolean retryFromStep4 = true;
        EditPayload payload = null;
        ItineraryItem before = null;

        while (retryFromStep4) {
            retryFromStep4 = false;

            // ----- Step 4: return current schedule with editable form -----
            before = itineraryRepository.loadEditableForm(selectedItemId);                         // Step 4

            // ----- Step 5: operator submits modification fields -----
            // A1 loop point: re-prompt on validation failure
            boolean retryFromStep5 = true;
            while (retryFromStep5) {
                retryFromStep5 = false;

                try {
                    payload = waitForSubmissionOrCancel(operator, before);                         // Step 5
                } catch (EditCancelledException ce) {
                    // A5: cancel between Step 4 and Step 7
                    // A5.1: discard temporary payload — local payload reference dropped
                    payload = null;                                                                // A5.1
                    // A5.2: release edit lock — handled by outer finally (lockOwned flag below)
                    itineraryRepository.releaseLock(selectedItemId);                               // A5.2
                    // A5.3: end use case — TODO: cancel response shape not specified
                    return null;                                                                   // A5.3 (TODO)
                }

                // ----- Step 6: validate payload (A1 on failure) -----
                try {
                    validate(payload, before);                                                     // Step 6
                } catch (ValidationException ve) {
                    // A1: reject with field-level error markers, return to Step 5
                    // TODO: field-error-marker propagation back to operator UI not specified
                    notifyValidationErrors(operator, ve.getFieldErrors());                         // A1
                    retryFromStep5 = true;                                                         // A1 → Step 5
                }
            }

            // ----- Step 7: write new revision (A2 on optimistic lock conflict) -----
            try {
                updated = itineraryRepository.writeNewRevision(selectedItemId,
                                                               payload,
                                                               before.getVersion());               // Step 7
            } catch (OptimisticLockConflictException oce) {
                // A2: abort write, reload latest, show merge view, return to Step 4
                ItineraryItem latest = itineraryRepository.reloadLatest(selectedItemId);           // A2
                showMergeView(operator, latest, payload);                                          // A2
                retryFromStep4 = true;                                                             // A2 → Step 4
            }
        }

        // ----- Step 8: increment version field by 1 -----
        updated = updated.withIncrementedVersion();                                                // Step 8

        // ----- Step 9: update assigned staff association -----
        updated = updated.withAssignedStaff(payload.getAssignedStaffIds());                        // Step 9

        // ----- Step 10: include Audit Log Action (operator_id, diff, ISO 8601 timestamp) -----
        List<JsonPatchEntry> diff = computeDiff(before, updated);                                  // Step 10
        auditLogAction.run(operator.getOperatorId(), diff, Instant.now());                         // Step 10

        // ----- Step 11: include Synchronize Realtime State (A3 on failure) -----
        boolean degradedSync = false;
        boolean syncOk = synchronizeRealtimeState.publish(updated);                                // Step 11
        if (!syncOk) {
            // A3.1: record delta to delayed dispatch queue
            delayedDispatchQueue.recordDelta(updated);                                             // A3.1
            // A3.2: set degraded_sync flag
            degradedSync = true;                                                                   // A3.2
            // A3.3: continue to end with degraded_sync flag set
        }

        // ----- Step 12: include Send Push Notification (A4 on failure) -----
        List<String> staffIds = updated.getAssignedStaffIds();
        String patientMagicLinkEndpoint = resolvePatientMagicLinkEndpoint(updated);                // Step 12
        boolean pushOk = sendPushNotification.queue(staffIds, patientMagicLinkEndpoint);           // Step 12
        if (!pushOk) {
            // A4.1: record failure to alert center
            alertCenter.recordFailure(staffIds, patientMagicLinkEndpoint);                         // A4.1
            // A4.2: continue to Step 13
        }

        // ----- Step 13: release edit lock -----
        itineraryRepository.releaseLock(selectedItemId);                                           // Step 13

        // ----- Step 14: return updated itinerary view to operator -----
        Object updatedView = toItineraryView(updated);                                             // Step 14
        EditItineraryResponse response = EditItineraryResponse.success(updatedView, degradedSync); // Step 14

        // ----- Step 15: return to dashboard ready state (§3 definition) -----
        // §3: lock released + new fetch possible + ready for next operator input
        // TODO: dashboard ready-state transition mechanism not specified
        return response;                                                                           // Step 15
    }

    // ----- Helpers (each maps to a Description step / alternative; bodies are TODO) -----

    // Step 2: operator selection input mechanism
    private String waitForOperatorSelection(AgencyOperator operator) {
        // TODO: selection input mechanism not specified in Description
        return null;
    }

    // Step 5 / A5 detection
    private EditPayload waitForSubmissionOrCancel(AgencyOperator operator, ItineraryItem before)
            throws EditCancelledException {
        // TODO: submission/cancel signal mechanism not specified in Description
        return null;
    }

    // Step 6: validate payload (schema, business rules, sibling time-overlap)
    private void validate(EditPayload payload, ItineraryItem before) throws ValidationException {
        // TODO: validation rules not specified beyond
        //       "페이로드 스키마, 비즈니스 규칙, 형제 항목과의 시간대 중복"
    }

    // A1: surface field-level error markers
    private void notifyValidationErrors(AgencyOperator operator, Map<String, String> fieldErrors) {
        // TODO: error-surface mechanism not specified in Description
    }

    // A2: present merge view of latest revision vs operator payload
    private void showMergeView(AgencyOperator operator, ItineraryItem latest, EditPayload payload) {
        // TODO: merge view presentation not specified in Description
    }

    // Step 10: compute JSON Patch diff between before and after (per §2)
    private List<JsonPatchEntry> computeDiff(ItineraryItem before, ItineraryItem after) {
        // TODO: diff generation rules not specified beyond §2 format
        return null;
    }

    // Step 12: resolve patient magic-link endpoint for assigned itinerary
    private String resolvePatientMagicLinkEndpoint(ItineraryItem item) {
        // TODO: patient↔itinerary link resolution not specified in Description
        return null;
    }

    // Step 14: build updated itinerary view representation
    private Object toItineraryView(ItineraryItem updated) {
        // TODO: view shape not specified in Description
        return updated;
    }
}
