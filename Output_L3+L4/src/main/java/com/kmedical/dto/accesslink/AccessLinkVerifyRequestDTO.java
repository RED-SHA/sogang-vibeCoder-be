package com.kmedical.dto.accesslink;

import java.time.LocalDate;

/** Interface → AccessLinkController 간 매직링크 검증 요청 DTO */
public class AccessLinkVerifyRequestDTO {

    private String token;
    private LocalDate dateOfBirth;

    public AccessLinkVerifyRequestDTO() {}

    public AccessLinkVerifyRequestDTO(String token, LocalDate dateOfBirth) {
        this.token = token;
        this.dateOfBirth = dateOfBirth;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
}
