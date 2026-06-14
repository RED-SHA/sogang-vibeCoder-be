package com.kmedical.dto.journey;

import com.kmedical.domain.enums.ScheduleItemStatus;
import com.kmedical.domain.enums.ScheduleItemType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Interface → JourneyController 간 일정 수정 요청 DTO */
public class ScheduleItemUpdateRequestDTO {

    private String scheduleItemId;
    private String patientJourneyId;
    private String operatorId;
    private Integer expectedVersion;
    private Boolean cancelRequested;
    private ScheduleItemType itemType;
    private String title;
    private LocalDateTime scheduledStartAt;
    private LocalDateTime scheduledEndAt;
    private String locationAddressEn;
    private BigDecimal locationCoordLat;
    private BigDecimal locationCoordLng;
    private ScheduleItemStatus status;
    private Boolean isCritical;
    private String memo;
    private Integer sortOrder;
    private List<String> assignedStaffIds = new ArrayList<>();
    private String patientMagicLinkEndpoint;

    public ScheduleItemUpdateRequestDTO() {}

    public String getScheduleItemId() { return scheduleItemId; }
    public void setScheduleItemId(String scheduleItemId) { this.scheduleItemId = scheduleItemId; }

    public String getPatientJourneyId() { return patientJourneyId; }
    public void setPatientJourneyId(String patientJourneyId) { this.patientJourneyId = patientJourneyId; }

    public String getOperatorId() { return operatorId; }
    public void setOperatorId(String operatorId) { this.operatorId = operatorId; }

    public Integer getExpectedVersion() { return expectedVersion; }
    public void setExpectedVersion(Integer expectedVersion) { this.expectedVersion = expectedVersion; }

    public Boolean getCancelRequested() { return cancelRequested; }
    public void setCancelRequested(Boolean cancelRequested) { this.cancelRequested = cancelRequested; }

    public ScheduleItemType getItemType() { return itemType; }
    public void setItemType(ScheduleItemType itemType) { this.itemType = itemType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDateTime getScheduledStartAt() { return scheduledStartAt; }
    public void setScheduledStartAt(LocalDateTime scheduledStartAt) { this.scheduledStartAt = scheduledStartAt; }

    public LocalDateTime getScheduledEndAt() { return scheduledEndAt; }
    public void setScheduledEndAt(LocalDateTime scheduledEndAt) { this.scheduledEndAt = scheduledEndAt; }

    public String getLocationAddressEn() { return locationAddressEn; }
    public void setLocationAddressEn(String locationAddressEn) { this.locationAddressEn = locationAddressEn; }

    public BigDecimal getLocationCoordLat() { return locationCoordLat; }
    public void setLocationCoordLat(BigDecimal locationCoordLat) { this.locationCoordLat = locationCoordLat; }

    public BigDecimal getLocationCoordLng() { return locationCoordLng; }
    public void setLocationCoordLng(BigDecimal locationCoordLng) { this.locationCoordLng = locationCoordLng; }

    public ScheduleItemStatus getStatus() { return status; }
    public void setStatus(ScheduleItemStatus status) { this.status = status; }

    public Boolean getIsCritical() { return isCritical; }
    public void setIsCritical(Boolean isCritical) { this.isCritical = isCritical; }

    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public List<String> getAssignedStaffIds() {
        return Collections.unmodifiableList(assignedStaffIds);
    }

    public void setAssignedStaffIds(List<String> assignedStaffIds) {
        this.assignedStaffIds = assignedStaffIds == null
                ? new ArrayList<>()
                : new ArrayList<>(assignedStaffIds);
    }

    public String getPatientMagicLinkEndpoint() { return patientMagicLinkEndpoint; }
    public void setPatientMagicLinkEndpoint(String patientMagicLinkEndpoint) {
        this.patientMagicLinkEndpoint = patientMagicLinkEndpoint;
    }
}
