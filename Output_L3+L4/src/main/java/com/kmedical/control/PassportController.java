package com.kmedical.control;

import com.kmedical.adapter.OCRAdapter;
import com.kmedical.domain.entity.PassportInfo;
import com.kmedical.domain.enums.PassportReviewStatus;
import com.kmedical.dto.passport.PassportInfoDTO;
import com.kmedical.dto.passport.PassportReviewRequestDTO;
import com.kmedical.dto.passport.PassportUploadRequestDTO;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * SRV-C03 — PassportController
 * 책임: OCR 요청 디스패치, 결과 저장, 검토 워크플로우.
 * UC: UC-X03, UC-P03, UC-A03
 */
public class PassportController {

    private final OCRAdapter ocrAdapter;
    private final Map<String, PassportInfo> passportStore = new HashMap<>();

    public PassportController(OCRAdapter ocrAdapter) {
        this.ocrAdapter = ocrAdapter;
    }

    private void guardNotClosedDown() {
        if (SystemStateRegistry.getInstance().isClosedDown()) {
            throw new IllegalStateException("System is closed down. Customer operations are not permitted.");
        }
    }

    /**
     * 여권 이미지 업로드 후 OCR을 실행하여 결과를 저장한다.
     * System Response: OCR 요청 → 결과 파싱 → PassportInfo 저장(PENDING)
     */
    public PassportInfoDTO uploadAndOcr(PassportUploadRequestDTO request) {
        guardNotClosedDown();
        if (request == null || request.getPatientId() == null || request.getImageUrl() == null) {
            throw new IllegalArgumentException("Passport upload request is incomplete.");
        }

        PassportInfoDTO ocrResult = ocrAdapter.extractPassportInfo(request.getImageUrl());

        PassportInfo info = new PassportInfo();
        info.setPassportInfoId(UUID.randomUUID().toString());
        info.setPatientId(request.getPatientId());
        info.setImageUrl(request.getImageUrl());
        info.setOcrFullNameEn(ocrResult.getOcrFullNameEn());
        info.setOcrPassportNumber(ocrResult.getOcrPassportNumber());
        info.setOcrNationality(ocrResult.getOcrNationality());
        info.setOcrExpiryDate(ocrResult.getOcrExpiryDate());
        info.setOcrConfidence(ocrResult.getOcrConfidence());
        info.setReviewStatus(PassportReviewStatus.PENDING);

        passportStore.put(info.getPassportInfoId(), info);
        return toDTO(info);
    }

    /**
     * 관리자가 OCR 결과를 검토하여 APPROVED 또는 REJECTED로 확정한다.
     * System Response: 입력 검증 → 상태 전이 → 확정 정보 저장
     */
    public PassportInfoDTO reviewPassport(PassportReviewRequestDTO request) {
        guardNotClosedDown();
        if (request == null || request.getPassportInfoId() == null) {
            throw new IllegalArgumentException("Review request is incomplete.");
        }

        PassportInfo info = passportStore.get(request.getPassportInfoId());
        if (info == null) throw new IllegalArgumentException("PassportInfo not found: " + request.getPassportInfoId());
        if (info.getReviewStatus() != PassportReviewStatus.PENDING) {
            throw new IllegalStateException("PassportInfo is not in PENDING state.");
        }

        info.setReviewedBy(request.getReviewedBy());
        info.setReviewedAt(LocalDateTime.now());
        info.setReviewStatus(request.getReviewStatus());

        if (request.getReviewStatus() == PassportReviewStatus.APPROVED) {
            info.setConfirmedFullNameEn(request.getConfirmedFullNameEn());
            info.setConfirmedPassportNumber(request.getConfirmedPassportNumber());
        } else {
            info.setRejectionReason(request.getRejectionReason());
        }

        return toDTO(info);
    }

    /**
     * 환자의 여권 정보를 조회한다.
     */
    public PassportInfoDTO getPassportInfo(String passportInfoId) {
        guardNotClosedDown();
        PassportInfo info = passportStore.get(passportInfoId);
        if (info == null) throw new IllegalArgumentException("PassportInfo not found: " + passportInfoId);
        return toDTO(info);
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private PassportInfoDTO toDTO(PassportInfo info) {
        PassportInfoDTO dto = new PassportInfoDTO();
        dto.setPassportInfoId(info.getPassportInfoId());
        dto.setPatientId(info.getPatientId());
        dto.setImageUrl(info.getImageUrl());
        dto.setOcrFullNameEn(info.getOcrFullNameEn());
        dto.setOcrPassportNumber(info.getOcrPassportNumber());
        dto.setOcrNationality(info.getOcrNationality());
        dto.setOcrExpiryDate(info.getOcrExpiryDate());
        dto.setOcrConfidence(info.getOcrConfidence());
        dto.setConfirmedFullNameEn(info.getConfirmedFullNameEn());
        dto.setConfirmedPassportNumber(info.getConfirmedPassportNumber());
        dto.setReviewStatus(info.getReviewStatus());
        dto.setReviewedBy(info.getReviewedBy());
        dto.setReviewedAt(info.getReviewedAt());
        dto.setRejectionReason(info.getRejectionReason());
        return dto;
    }
}
