package com.kmedical.dto.auth;

/** AuthController → Interface 간 OAuth 로그인 응답 전달 DTO */
public class OAuthLoginResponseDTO {

    private String sessionToken;
    private String userId;
    private String userType;
    private boolean isNewUser;

    public OAuthLoginResponseDTO() {}

    public OAuthLoginResponseDTO(String sessionToken, String userId, String userType, boolean isNewUser) {
        this.sessionToken = sessionToken;
        this.userId = userId;
        this.userType = userType;
        this.isNewUser = isNewUser;
    }

    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public boolean isNewUser() { return isNewUser; }
    public void setNewUser(boolean newUser) { isNewUser = newUser; }
}
