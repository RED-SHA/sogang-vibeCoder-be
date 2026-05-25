package com.kmedical.control;

import com.kmedical.adapter.OCRAdapter;
import com.kmedical.domain.entity.PassportInfo;
import com.kmedical.domain.enums.PassportReviewStatus;
import com.kmedical.dto.passport.PassportInfoDTO;
import com.kmedical.dto.passport.PassportReviewRequestDTO;
import com.kmedical.dto.passport.PassportUploadRequestDTO;
import com.kmedical.util.AuditLogger;
import com.kmedical.util.MaskingUtil;
import com.kmedical.util.ValidationUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * SRV-C03 — PassportController
 * 책임: OCR 요청 디스패치, 결과 저장, 검토 워크플로우.
 * UC: UC-X03, UC-P03, UC-A03
 * NFR 적용: ConcurrentHashMap, HTTPS imageUrl 검증, AuditLogger(PASSPORT_REVIEW), PERF 로깅(3000ms)
 */
public class PassportController {

    private static final long OCR_PERF_THRESHOLD_MS = 3000L;

    private final OCRAdapter ocrAdapter;
    private final Map<String, PassportInfo> passportStore = new ConcurrentHashMap<>();

    public PassportController(OCRAdapter ocrAdapter) {
        this.ocrAdapter = ocrAdapter;
    }

    private void guardNotClosedDown() {
        if (SystemStateRegistry.getInstance().isClosedDown()) {
            AuditLogger.closedDownAccess("PassportController", "UNKNOWN");
            throw new IllegalStateException("System is closed down. Customer operations are not permitted.");
        }
    }

    /**
     * 여권 이미지 업로드 후 OCR을 실행하여 결과를 저장한다.
     * 검증: patientId not null, imageUrl HTTPS 필수
     * NFR-PERF: OCR 처리 시간 3000ms 임계값 측정
     */
    public PassportInfoDTO uploadAndOcr(PassportUploadRequestDTO request) {
        guardNotClosedDown();
        ValidationUtil.requireNotNull(request, "PassportUploadRequestDTO");
        ValidationUtil.requireNotBlank(request.getPatientId(), "patientId");
        ValidationUtil.requireHttpsUrl(request.getImageUrl(), "imageUrl");

        long start = System.currentTimeMillis();
        try {
            PassportInfoDTO ocrResult = ocrAdapter.extractPassportInfo(request.getImageUrl());

            PassportInfo info = new PassportInfo();
            info.setPassportInfoId(UUID.randomUUID().toString());
            info.setPatientId(request.getPatientId());
            info.setImageUrl(request.getImageUrl());
            info.setOcrFullNameEn(ocrResult.getOcrFullNameEn());
            info.setOcrPassportNumber(ocrResult.getOcrPassportNumber());
            info.setOcrNationality(ocrResult.getOcrNationality());
            if (ocrResult.getOcrNationality() != null &&
                    !ocrResult.getOcrNationality().matches("^[A-Z]{3}$")) {
                AuditLogger.warn("PASSPORT_NATIONALITY_FORMAT",
                        "patientId=" + request.getPatientId() +
                        " ocrNationality=" + ocrResult.getOcrNationality());
            }
            info.setOcrExpiryDate(ocrResult.getOcrExpiryDate());
            info.setOcrConfidence(ocrResult.getOcrConfidence());
            info.setReviewStatus(PassportReviewStatus.PENDING);

            // 만료일 경고 로그 (과거 날짜 → 만료 가능성)
            if (ocrResult.getOcrExpiryDate() != null && ocrResult.getOcrExpiryDate().isBefore(LocalDate.now())) {
                AuditLogger.warn("PASSPORT_MAY_EXPIRED",
                        "patientId=" + request.getPatientId() + " expiryDate=" + ocrResult.getOcrExpiryDate());
            }
            // OCR 신뢰도 낮음 경고
            if (ocrResult.getOcrConfidence() != null &&
                    ocrResult.getOcrConfidence().compareTo(new java.math.BigDecimal("0.70")) < 0) {
                AuditLogger.warn("OCR_LOW_CONFIDENCE",
                        "patientId=" + request.getPatientId() + " confidence=" + ocrResult.getOcrConfidence());
            }

            passportStore.put(info.getPassportInfoId(), info);
            AuditLogger.log("PASSPORT_OCR_UPLOAD", request.getPatientId(),
                    MaskingUtil.maskUrl(request.getImageUrl()), true,
                    "ocrConfidence=" + ocrResult.getOcrConfidence());
            return toDTO(info);

        } finally {
            AuditLogger.perf("PASSPORT_OCR_UPLOAD", System.currentTimeMillis() - start, OCR_PERF_THRESHOLD_MS);
        }
    }

    /**
     * 관리자가 OCR 결과를 검토하여 APPROVED 또는 REJECTED로 확정한다.
     * 검증: APPROVED 시 confirmedFullNameEn·confirmedPassportNumber 필수,
     *       REJECTED 시 rejectionReason 필수
     * NFR-LOG: PASSPORT_REVIEW 감사 로그
     */
    public PassportInfoDTO reviewPassport(PassportReviewRequestDTO request) {
        guardNotClosedDown();
        ValidationUtil.requireNotNull(request, "PassportReviewRequestDTO");
        ValidationUtil.requireNotBlank(request.getPassportInfoId(), "passportInfoId");
        ValidationUtil.requireNotNull(request.getReviewStatus(), "reviewStatus");
        ValidationUtil.requireNotBlank(request.getReviewedBy(), "reviewedBy");

        PassportInfo info = passportStore.get(request.getPassportInfoId());
        if (info == null)
            throw new IllegalArgumentException("PassportInfo not found: " + request.getPassportInfoId());
        if (info.getReviewStatus() != PassportReviewStatus.PENDING)
            throw new IllegalStateException("PassportInfo is not in PENDING state.");

        try {
            if (request.getReviewStatus() == PassportReviewStatus.APPROVED) {
                ValidationUtil.requireValidFullNameEn(request.getConfirmedFullNameEn(), "confirmedFullNameEn");
                ValidationUtil.requireValidPassportNumber(request.getConfirmedPassportNumber(), "confirmedPassportNumber");
                info.setConfirmedFullNameEn(request.getConfirmedFullNameEn());
                info.setConfirmedPassportNumber(request.getConfirmedPassportNumber());
            } else if (request.getReviewStatus() == PassportReviewStatus.REJECTED) {
                ValidationUtil.requireNotBlank(request.getRejectionReason(), "rejectionReason");
                ValidationUtil.requireMaxLength(request.getRejectionReason(), 500, "rejectionReason");
                info.setRejectionReason(request.getRejectionReason());
            }

            info.setReviewedBy(request.getReviewedBy());
            info.setReviewedAt(LocalDateTime.now());
            info.setReviewStatus(request.getReviewStatus());

            AuditLogger.log("PASSPORT_REVIEW", request.getReviewedBy(), request.getPassportInfoId(), true,
                    "status=" + request.getReviewStatus());
            return toDTO(info);

        } catch (Exception e) {
            AuditLogger.log("PASSPORT_REVIEW", request.getReviewedBy(), request.getPassportInfoId(), false, e.getMessage());
            if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) throw e;
            throw new IllegalStateException("Passport review failed: " + e.getMessage());
        }
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
