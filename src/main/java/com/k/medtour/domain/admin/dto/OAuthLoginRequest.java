package com.k.medtour.domain.admin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OAuthLoginRequest(
        @NotBlank(message = "idToken은 필수입니다.")
        String idToken,

        @Valid
        @NotNull(message = "deviceInfo는 필수입니다.")
        DeviceInfo deviceInfo
) {
    public record DeviceInfo(
            @NotBlank(message = "platform은 필수입니다.")
            String platform,

            String language
    ) {
    }
}
