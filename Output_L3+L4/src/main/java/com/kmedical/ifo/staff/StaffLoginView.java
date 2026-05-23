package com.kmedical.ifo.staff;

import com.kmedical.control.AuthController;
import com.kmedical.dto.auth.OAuthLoginRequestDTO;
import com.kmedical.dto.auth.OAuthLoginResponseDTO;

/**
 * IFO-S01 — StaffLoginView
 * UC: UC-S01 (스태프 로그인)
 * 책임: 스태프가 OAuth를 통해 로그인하는 UI 진입점.
 */
public class StaffLoginView {

    private final AuthController authController;

    public StaffLoginView(AuthController authController) {
        this.authController = authController;
    }

    /**
     * 스태프가 OAuth 로그인을 수행한다.
     * Actor Action: Staff member initiates OAuth login.
     */
    public OAuthLoginResponseDTO login(OAuthLoginRequestDTO request) {
        return authController.loginWithOAuth(request);
    }
}
