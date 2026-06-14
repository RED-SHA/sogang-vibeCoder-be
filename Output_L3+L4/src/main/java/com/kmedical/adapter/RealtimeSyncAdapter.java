package com.kmedical.adapter;

import com.kmedical.dto.journey.ScheduleItemDTO;

/**
 * INF-A06 — UC-ADM-07 실시간 일정 스냅샷 동기화 어댑터.
 */
public interface RealtimeSyncAdapter {

    /**
     * 수정된 일정 스냅샷을 구독 중인 환자와 실무자 클라이언트에 발행한다.
     *
     * @param snapshot 갱신된 일정 항목 스냅샷
     * @return 발행 성공 여부
     */
    boolean publishScheduleSnapshot(ScheduleItemDTO snapshot);
}
