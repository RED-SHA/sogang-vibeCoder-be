package com.kmedical.domain.enums;

/** UC-ADM-07 edit session statechart states. */
public enum EditItineraryStatus {
    INIT,
    AUTH_CHECKED,
    ITEM_SELECTED,
    LOCK_ACQUIRED,
    EDIT_FORM_RETURNED,
    SUBMITTED,
    VALIDATED,
    PERSISTED,
    AUDIT_LOGGED,
    SYNC_REQUESTED,
    SYNC_DEGRADED,
    PUSH_REQUESTED,
    READY,
    CANCELLED,
    REJECTED,
    FAILED
}
