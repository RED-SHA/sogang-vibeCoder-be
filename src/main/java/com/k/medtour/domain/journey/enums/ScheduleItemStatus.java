package com.k.medtour.domain.journey.enums;

import java.util.Set;

public enum ScheduleItemStatus {
    SCHEDULED,
    EN_ROUTE,
    ARRIVED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    private static final Set<ScheduleItemStatus> FORWARD_ORDER = Set.of(
            SCHEDULED, EN_ROUTE, ARRIVED, IN_PROGRESS, COMPLETED
    );

    public boolean canTransitionTo(ScheduleItemStatus next) {
        if (next == CANCELLED) {
            return this != COMPLETED && this != CANCELLED;
        }
        if (this == CANCELLED || this == COMPLETED) {
            return false;
        }
        return this.ordinal() < next.ordinal() && FORWARD_ORDER.contains(next);
    }
}
