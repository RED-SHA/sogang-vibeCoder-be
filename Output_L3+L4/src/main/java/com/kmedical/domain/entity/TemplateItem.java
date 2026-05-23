package com.kmedical.domain.entity;

import com.kmedical.domain.enums.ScheduleItemType;
import com.kmedical.domain.enums.StaffRole;

/** C32 — TemplateItem «entity» (ItineraryTemplate 구성 항목) */
public class TemplateItem {

    private String templateItemId;
    private String itineraryTemplateId;
    private ScheduleItemType itemType;
    private String title;
    private Integer durationMinutes;
    private StaffRole assignedStaffRole;
    private Integer sortOrder;

    public TemplateItem() {}

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
