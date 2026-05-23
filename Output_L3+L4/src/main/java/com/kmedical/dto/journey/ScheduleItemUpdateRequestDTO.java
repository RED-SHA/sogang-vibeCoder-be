package com.kmedical.dto.journey;

import com.kmedical.domain.enums.ScheduleItemStatus;
import com.kmedical.domain.enums.ScheduleItemType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Interface → JourneyController 간 일정 수정 요청 DTO */
public class ScheduleItemUpdateRequestDTO {

    private String scheduleItemId;
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

    public ScheduleItemUpdateRequestDTO() {}

    public String getScheduleItemId() { return scheduleItemId; }
    public void setScheduleItemId(String scheduleItemId) { this.scheduleItemId = scheduleItemId; }

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
}
