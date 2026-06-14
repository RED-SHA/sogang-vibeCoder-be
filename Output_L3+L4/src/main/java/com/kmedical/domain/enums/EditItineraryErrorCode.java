package com.kmedical.domain.enums;

/** UC-ADM-07 L5 alternative result codes. */
public enum EditItineraryErrorCode {
    VALIDATION_FAILED,
    OPTIMISTIC_LOCK_CONFLICT,
    REALTIME_SYNC_DEGRADED,
    PUSH_NOTIFICATION_FAILED,
    EDIT_CANCELLED,
    PERMISSION_DENIED,
    LOCK_HELD
}
