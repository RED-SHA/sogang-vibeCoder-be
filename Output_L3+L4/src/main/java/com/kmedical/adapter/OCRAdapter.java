package com.kmedical.adapter;

import com.kmedical.dto.passport.PassportInfoDTO;

/**
 * INF-A05 — OCRAdapter
 * E10 PassportOCRService 연동 인터페이스.
 * SRV-C03 PassportController가 호출한다.
 */
public interface OCRAdapter {

    /**
     * 여권 이미지 URL을 OCR 서비스에 전송하여 추출 결과를 반환한다.
     *
     * @param imageUrl 여권 이미지 URL
     * @return OCR 추출 결과 (DTO 형태, ocrFullNameEn·ocrPassportNumber·ocrNationality·ocrExpiryDate·ocrConfidence 포함)
     * @throws RuntimeException OCR 서비스 오류 시
     */
    PassportInfoDTO extractPassportInfo(String imageUrl);
}
