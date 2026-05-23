package com.kmedical.domain.enums;

/**
 * EN18 — 접속 링크 유형
 * 만료 기간: PATIENT_GUEST_VIEW·PATIENT_PROPOSAL 72시간,
 *           STAFF_INVITATION 24시간, INVOICE_VIEW 30일
 */
public enum AccessLinkType {
    PATIENT_GUEST_VIEW, PATIENT_PROPOSAL, STAFF_INVITATION, INVOICE_VIEW
}
