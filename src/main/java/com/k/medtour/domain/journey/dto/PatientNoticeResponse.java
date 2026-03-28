package com.k.medtour.domain.journey.dto;

import java.util.List;

public record PatientNoticeResponse(
        Long patientId,
        String patientName,
        String nationality,
        String language,
        List<String> allergies,
        String specialNotes,
        EmergencyContactDto emergencyContact
) {
    public record EmergencyContactDto(
            String name,
            String relationship,
            String phone
    ) {
    }
}
