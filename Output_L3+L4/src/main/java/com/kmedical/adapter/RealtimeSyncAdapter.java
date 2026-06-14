package com.kmedical.adapter;

import com.kmedical.dto.journey.ScheduleItemDTO;

/** INF-A05 - UC-ADM-07 updated itinerary snapshot publisher. */
public interface RealtimeSyncAdapter {

    boolean publishScheduleSnapshot(ScheduleItemDTO snapshot);
}
