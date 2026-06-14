package com.kmedical.dto.journey;

import com.kmedical.domain.enums.ScheduleItemStatus;
import com.kmedical.domain.enums.ScheduleItemType;
import com.kmedical.domain.enums.UserRoleName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Aggregate request DTO for UC-ADM-07 Edit Itinerary. */
public final class EditItineraryRequestDTO {

    private final String operatorId;
    private final UserRoleName operatorRole;
    private final String patientJourneyId;
    private final String scheduleItemId;
    private final Integer expectedVersion;
    private final boolean cancelRequested;
    private final ScheduleItemType itemType;
    private final String title;
    private final LocalDateTime scheduledStartAt;
    private final LocalDateTime scheduledEndAt;
    private final String locationAddressEn;
    private final BigDecimal locationCoordLat;
    private final BigDecimal locationCoordLng;
    private final ScheduleItemStatus status;
    private final Boolean isCritical;
    private final String memo;
    private final Integer sortOrder;
    private final List<String> assignedStaffIds;

    public EditItineraryRequestDTO(String operatorId,
                                   UserRoleName operatorRole,
                                   String patientJourneyId,
                                   String scheduleItemId,
                                   Integer expectedVersion,
                                   boolean cancelRequested,
                                   ScheduleItemType itemType,
                                   String title,
                                   LocalDateTime scheduledStartAt,
                                   LocalDateTime scheduledEndAt,
                                   String locationAddressEn,
                                   BigDecimal locationCoordLat,
                                   BigDecimal locationCoordLng,
                                   ScheduleItemStatus status,
                                   Boolean isCritical,
                                   String memo,
                                   Integer sortOrder,
                                   List<String> assignedStaffIds) {
        this.operatorId = operatorId;
        this.operatorRole = operatorRole;
        this.patientJourneyId = patientJourneyId;
        this.scheduleItemId = scheduleItemId;
        this.expectedVersion = expectedVersion;
        this.cancelRequested = cancelRequested;
        this.itemType = itemType;
        this.title = title;
        this.scheduledStartAt = scheduledStartAt;
        this.scheduledEndAt = scheduledEndAt;
        this.locationAddressEn = locationAddressEn;
        this.locationCoordLat = locationCoordLat;
        this.locationCoordLng = locationCoordLng;
        this.status = status;
        this.isCritical = isCritical;
        this.memo = memo;
        this.sortOrder = sortOrder;
        this.assignedStaffIds = assignedStaffIds == null
                ? null
                : Collections.unmodifiableList(new ArrayList<>(assignedStaffIds));
    }

    public String getOperatorId() { return operatorId; }
    public UserRoleName getOperatorRole() { return operatorRole; }
    public String getPatientJourneyId() { return patientJourneyId; }
    public String getScheduleItemId() { return scheduleItemId; }
    public Integer getExpectedVersion() { return expectedVersion; }
    public boolean isCancelRequested() { return cancelRequested; }
    public ScheduleItemType getItemType() { return itemType; }
    public String getTitle() { return title; }
    public LocalDateTime getScheduledStartAt() { return scheduledStartAt; }
    public LocalDateTime getScheduledEndAt() { return scheduledEndAt; }
    public String getLocationAddressEn() { return locationAddressEn; }
    public BigDecimal getLocationCoordLat() { return locationCoordLat; }
    public BigDecimal getLocationCoordLng() { return locationCoordLng; }
    public ScheduleItemStatus getStatus() { return status; }
    public Boolean getIsCritical() { return isCritical; }
    public String getMemo() { return memo; }
    public Integer getSortOrder() { return sortOrder; }
    public List<String> getAssignedStaffIds() { return assignedStaffIds; }
}
