package com.kmedical.dto.journey;

import com.kmedical.domain.enums.EditItineraryErrorCode;
import com.kmedical.domain.enums.EditItineraryState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Aggregate response DTO for UC-ADM-07 Edit Itinerary. */
public final class EditItineraryResponseDTO {

    private final boolean success;
    private final boolean cancelled;
    private final ScheduleItemDTO updatedScheduleItem;
    private final ScheduleItemDTO latestScheduleItem;
    private final List<String> assignedStaffIds;
    private final boolean degradedSync;
    private final boolean pushWarning;
    private final EditItineraryErrorCode errorCode;
    private final String errorDetail;
    private final String lockHolderId;
    private final EditItineraryState state;

    private EditItineraryResponseDTO(boolean success,
                                     boolean cancelled,
                                     ScheduleItemDTO updatedScheduleItem,
                                     ScheduleItemDTO latestScheduleItem,
                                     List<String> assignedStaffIds,
                                     boolean degradedSync,
                                     boolean pushWarning,
                                     EditItineraryErrorCode errorCode,
                                     String errorDetail,
                                     String lockHolderId,
                                     EditItineraryState state) {
        this.success = success;
        this.cancelled = cancelled;
        this.updatedScheduleItem = updatedScheduleItem;
        this.latestScheduleItem = latestScheduleItem;
        this.assignedStaffIds = assignedStaffIds == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(assignedStaffIds));
        this.degradedSync = degradedSync;
        this.pushWarning = pushWarning;
        this.errorCode = errorCode;
        this.errorDetail = errorDetail;
        this.lockHolderId = lockHolderId;
        this.state = state;
    }

    public static EditItineraryResponseDTO success(ScheduleItemDTO updatedScheduleItem,
                                                   List<String> assignedStaffIds,
                                                   boolean degradedSync,
                                                   boolean pushWarning,
                                                   EditItineraryState state) {
        return new EditItineraryResponseDTO(true, false, updatedScheduleItem, null, assignedStaffIds,
                degradedSync, pushWarning, null, null, null, state);
    }

    public static EditItineraryResponseDTO cancelled(EditItineraryState state) {
        return new EditItineraryResponseDTO(false, true, null, null, null,
                false, false, null, "Edit cancelled.", null, state);
    }

    public static EditItineraryResponseDTO error(EditItineraryErrorCode errorCode,
                                                 String errorDetail,
                                                 String lockHolderId,
                                                 ScheduleItemDTO latestScheduleItem,
                                                 EditItineraryState state) {
        return new EditItineraryResponseDTO(false, false, null, latestScheduleItem, null,
                false, false, errorCode, errorDetail, lockHolderId, state);
    }

    public boolean isSuccess() { return success; }
    public boolean isCancelled() { return cancelled; }
    public ScheduleItemDTO getUpdatedScheduleItem() { return updatedScheduleItem; }
    public ScheduleItemDTO getLatestScheduleItem() { return latestScheduleItem; }
    public List<String> getAssignedStaffIds() { return assignedStaffIds; }
    public boolean isDegradedSync() { return degradedSync; }
    public boolean isPushWarning() { return pushWarning; }
    public EditItineraryErrorCode getErrorCode() { return errorCode; }
    public String getErrorDetail() { return errorDetail; }
    public String getLockHolderId() { return lockHolderId; }
    public EditItineraryState getState() { return state; }
}
