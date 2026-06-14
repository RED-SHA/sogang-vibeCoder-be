package com.kmedical.dto.journey;

import com.kmedical.domain.enums.EditItineraryErrorCode;
import com.kmedical.domain.enums.EditItineraryStatus;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** UC-ADM-07 Aggregate 응답 DTO. */
public final class EditItineraryResponseDTO {

    private final boolean success;
    private final boolean cancelled;
    private final boolean degradedSync;
    private final boolean pushWarning;
    private final EditItineraryErrorCode errorCode;
    private final String errorDetail;
    private final Map<String, String> fieldErrors;
    private final ScheduleItemDTO updatedScheduleItem;
    private final ScheduleItemDTO latestScheduleItem;
    private final ScheduleItemDTO submittedScheduleItem;
    private final List<String> assignedStaffIds;
    private final EditItineraryStatus status;

    private EditItineraryResponseDTO(boolean success,
                                     boolean cancelled,
                                     boolean degradedSync,
                                     boolean pushWarning,
                                     EditItineraryErrorCode errorCode,
                                     String errorDetail,
                                     Map<String, String> fieldErrors,
                                     ScheduleItemDTO updatedScheduleItem,
                                     ScheduleItemDTO latestScheduleItem,
                                     ScheduleItemDTO submittedScheduleItem,
                                     List<String> assignedStaffIds,
                                     EditItineraryStatus status) {
        this.success = success;
        this.cancelled = cancelled;
        this.degradedSync = degradedSync;
        this.pushWarning = pushWarning;
        this.errorCode = errorCode;
        this.errorDetail = errorDetail;
        this.fieldErrors = fieldErrors == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(fieldErrors));
        this.updatedScheduleItem = updatedScheduleItem;
        this.latestScheduleItem = latestScheduleItem;
        this.submittedScheduleItem = submittedScheduleItem;
        this.assignedStaffIds = assignedStaffIds == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(assignedStaffIds);
        this.status = status;
    }

    public static EditItineraryResponseDTO success(ScheduleItemDTO updatedScheduleItem,
                                                   List<String> assignedStaffIds,
                                                   boolean degradedSync,
                                                   boolean pushWarning,
                                                   EditItineraryStatus status) {
        return new EditItineraryResponseDTO(true, false, degradedSync, pushWarning, null, null,
                null, updatedScheduleItem, null, null, assignedStaffIds, status);
    }

    public static EditItineraryResponseDTO cancelled(EditItineraryStatus status) {
        return new EditItineraryResponseDTO(false, true, false, false,
                EditItineraryErrorCode.CANCELLED, "edit_cancelled", null,
                null, null, null, null, status);
    }

    public static EditItineraryResponseDTO error(EditItineraryErrorCode errorCode,
                                                 String errorDetail,
                                                 EditItineraryStatus status) {
        return new EditItineraryResponseDTO(false, false, false, false, errorCode, errorDetail,
                null, null, null, null, null, status);
    }

    public static EditItineraryResponseDTO validationError(Map<String, String> fieldErrors,
                                                           EditItineraryStatus status) {
        return new EditItineraryResponseDTO(false, false, false, false,
                EditItineraryErrorCode.VALIDATION_FAILED, "validation_failed",
                fieldErrors, null, null, null, null, status);
    }

    public static EditItineraryResponseDTO conflict(ScheduleItemDTO latestScheduleItem,
                                                    ScheduleItemDTO submittedScheduleItem,
                                                    String detail,
                                                    EditItineraryStatus status) {
        return new EditItineraryResponseDTO(false, false, false, false,
                EditItineraryErrorCode.OPTIMISTIC_LOCK_CONFLICT, detail,
                null, null, latestScheduleItem, submittedScheduleItem, null, status);
    }

    public boolean isSuccess() { return success; }
    public boolean isCancelled() { return cancelled; }
    public boolean isDegradedSync() { return degradedSync; }
    public boolean isPushWarning() { return pushWarning; }
    public EditItineraryErrorCode getErrorCode() { return errorCode; }
    public String getErrorDetail() { return errorDetail; }
    public Map<String, String> getFieldErrors() { return fieldErrors; }
    public ScheduleItemDTO getUpdatedScheduleItem() { return updatedScheduleItem; }
    public ScheduleItemDTO getLatestScheduleItem() { return latestScheduleItem; }
    public ScheduleItemDTO getSubmittedScheduleItem() { return submittedScheduleItem; }
    public List<String> getAssignedStaffIds() { return assignedStaffIds; }
    public EditItineraryStatus getStatus() { return status; }
}
