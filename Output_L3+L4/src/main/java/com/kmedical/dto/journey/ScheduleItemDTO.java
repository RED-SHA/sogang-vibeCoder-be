package com.kmedical.dto.journey;

import com.kmedical.domain.enums.ScheduleItemStatus;
import com.kmedical.domain.enums.ScheduleItemType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** ScheduleItem 도메인 복사 DTO — Interface 계층 노출용 */
public class ScheduleItemDTO {

    private String scheduleItemId;
    private String patientJourneyId;
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
    private Integer version;

    public ScheduleItemDTO() {}

    public String getScheduleItemId() { return scheduleItemId; }
    public void setScheduleItemId(String scheduleItemId) { this.scheduleItemId = scheduleItemId; }

    public String getPatientJourneyId() { return patientJourneyId; }
    public void setPatientJourneyId(String patientJourneyId) { this.patientJourneyId = patientJourneyId; }

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

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
