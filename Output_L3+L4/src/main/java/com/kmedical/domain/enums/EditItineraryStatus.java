package com.kmedical.domain.enums;

/** UC-ADM-07 편집 세션 상태. */
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
