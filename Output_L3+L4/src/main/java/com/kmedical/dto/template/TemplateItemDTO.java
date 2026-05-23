package com.kmedical.dto.template;

import com.kmedical.domain.enums.ScheduleItemType;
import com.kmedical.domain.enums.StaffRole;

/** TemplateItem 도메인 복사 DTO — Interface 계층 노출용 */
public class TemplateItemDTO {

    private String templateItemId;
    private String itineraryTemplateId;
    private ScheduleItemType itemType;
    private String title;
    private Integer durationMinutes;
    private StaffRole assignedStaffRole;
    private Integer sortOrder;

    public TemplateItemDTO() {}

    public String getTemplateItemId() { return templateItemId; }
    public void setTemplateItemId(String templateItemId) { this.templateItemId = templateItemId; }

    public String getItineraryTemplateId() { return itineraryTemplateId; }
    public void setItineraryTemplateId(String itineraryTemplateId) { this.itineraryTemplateId = itineraryTemplateId; }

    public ScheduleItemType getItemType() { return itemType; }
    public void setItemType(ScheduleItemType itemType) { this.itemType = itemType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public StaffRole getAssignedStaffRole() { return assignedStaffRole; }
    public void setAssignedStaffRole(StaffRole assignedStaffRole) { this.assignedStaffRole = assignedStaffRole; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
