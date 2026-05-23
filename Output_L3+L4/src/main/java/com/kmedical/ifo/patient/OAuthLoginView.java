package com.kmedical.ifo.patient;

import com.kmedical.control.AuthController;
import com.kmedical.dto.auth.OAuthLoginRequestDTO;
import com.kmedical.dto.auth.OAuthLoginResponseDTO;

/**
 * IFO-P02 — OAuthLoginView
 * UC: UC-P02 (OAuth 소셜 로그인)
 * 책임: 환자가 Google/Kakao OAuth를 통해 로그인하는 UI 진입점.
 */
public class OAuthLoginView {

    private final AuthController authController;

    public OAuthLoginView(AuthController authController) {
        this.authController = authController;
    }

    /**
     * 환자가 OAuth 로그인을 시도한다.
     * Actor Action: Patient initiates OAuth login with a provider token.
     */
    public OAuthLoginResponseDTO loginWithOAuth(OAuthLoginRequestDTO request) {
        return authController.loginWithOAuth(request);
    }
}
