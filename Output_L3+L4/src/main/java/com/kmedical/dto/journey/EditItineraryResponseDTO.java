package com.kmedical.dto.journey;

import com.kmedical.domain.enums.EditItineraryErrorCode;
import com.kmedical.domain.enums.EditItineraryStatus;
import com.kmedical.dto.staff.StaffAssignmentDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** UC-ADM-07 aggregate response DTO. */
public class EditItineraryResponseDTO {

    private final boolean success;
    private final ScheduleItemDTO updatedScheduleItem;
    private final List<StaffAssignmentDTO> assignments;
    private final boolean degradedSync;
    private final boolean pushWarning;
    private final EditItineraryErrorCode errorCode;
    private final String errorDetail;
    private final ScheduleItemDTO latestScheduleItem;
    private final ScheduleItemDTO submittedScheduleItem;
    private final String lockHolderId;
    private final EditItineraryStatus status;

    private EditItineraryResponseDTO(boolean success,
                                     ScheduleItemDTO updatedScheduleItem,
                                     List<StaffAssignmentDTO> assignments,
                                     boolean degradedSync,
                                     boolean pushWarning,
                                     EditItineraryErrorCode errorCode,
                                     String errorDetail,
                                     ScheduleItemDTO latestScheduleItem,
                                     ScheduleItemDTO submittedScheduleItem,
                                     String lockHolderId,
                                     EditItineraryStatus status) {
        this.success = success;
        this.updatedScheduleItem = updatedScheduleItem;
        this.assignments = assignments == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(assignments));
        this.degradedSync = degradedSync;
        this.pushWarning = pushWarning;
        this.errorCode = errorCode;
        this.errorDetail = errorDetail;
        this.latestScheduleItem = latestScheduleItem;
        this.submittedScheduleItem = submittedScheduleItem;
        this.lockHolderId = lockHolderId;
        this.status = status;
    }

    public static EditItineraryResponseDTO success(ScheduleItemDTO updatedScheduleItem,
                                                   List<StaffAssignmentDTO> assignments,
                                                   boolean degradedSync,
                                                   boolean pushWarning) {
        return new EditItineraryResponseDTO(true, updatedScheduleItem, assignments, degradedSync, pushWarning,
                null, null, null, null, null, EditItineraryStatus.READY);
    }

    public static EditItineraryResponseDTO error(EditItineraryErrorCode errorCode,
                                                 String errorDetail,
                                                 EditItineraryStatus status) {
        return new EditItineraryResponseDTO(false, null, null, false, false,
                errorCode, errorDetail, null, null, null, status);
    }

    public static EditItineraryResponseDTO validationFailed(String errorDetail, ScheduleItemDTO submittedScheduleItem) {
        return new EditItineraryResponseDTO(false, null, null, false, false,
                EditItineraryErrorCode.VALIDATION_FAILED, errorDetail, null, submittedScheduleItem,
                null, EditItineraryStatus.EDIT_FORM_RETURNED);
    }

    public static EditItineraryResponseDTO lockHeld(String lockHolderId) {
        return new EditItineraryResponseDTO(false, null, null, false, false,
                EditItineraryErrorCode.LOCK_HELD, "lock_held", null, null,
                lockHolderId, EditItineraryStatus.REJECTED);
    }

    public static EditItineraryResponseDTO versionConflict(ScheduleItemDTO latestScheduleItem,
                                                           ScheduleItemDTO submittedScheduleItem) {
        return new EditItineraryResponseDTO(false, null, null, false, false,
                EditItineraryErrorCode.OPTIMISTIC_LOCK_CONFLICT, "optimistic_lock_conflict",
                latestScheduleItem, submittedScheduleItem, null, EditItineraryStatus.REJECTED);
    }

    public boolean isSuccess() { return success; }
    public ScheduleItemDTO getUpdatedScheduleItem() { return updatedScheduleItem; }
    public List<StaffAssignmentDTO> getAssignments() { return assignments; }
    public boolean isDegradedSync() { return degradedSync; }
    public boolean isPushWarning() { return pushWarning; }
    public EditItineraryErrorCode getErrorCode() { return errorCode; }
    public String getErrorDetail() { return errorDetail; }
    public ScheduleItemDTO getLatestScheduleItem() { return latestScheduleItem; }
    public ScheduleItemDTO getSubmittedScheduleItem() { return submittedScheduleItem; }
    public String getLockHolderId() { return lockHolderId; }
    public EditItineraryStatus getStatus() { return status; }
}
