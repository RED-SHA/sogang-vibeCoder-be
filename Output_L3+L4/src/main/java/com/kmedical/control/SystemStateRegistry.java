package com.kmedical.control;

import com.kmedical.domain.enums.SystemState;

/**
 * 시스템 운영 상태 레지스트리.
 * AuthController가 startUp()/closeDown()으로 상태를 갱신하고,
 * 모든 Control 메서드의 guardNotClosedDown()이 이를 조회한다.
 */
public final class SystemStateRegistry {

    private static final SystemStateRegistry INSTANCE = new SystemStateRegistry();

    private volatile SystemState state = SystemState.RUNNING;

    private SystemStateRegistry() {}

    public static SystemStateRegistry getInstance() {
        return INSTANCE;
    }

    public SystemState getState() {
        return state;
    }

    void setState(SystemState state) {
        this.state = state;
    }

    public boolean isClosedDown() {
        return state == SystemState.CLOSED_DOWN;
    }
}
