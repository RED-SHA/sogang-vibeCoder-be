package com.k.medtour.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_001", "잘못된 입력입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_002", "리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_003", "서버 내부 오류가 발생했습니다."),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "COMMON_004", "이미 존재하는 리소스입니다."),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_001", "인증이 필요합니다."),
    INVALID_OAUTH_TOKEN(HttpStatus.BAD_REQUEST, "AUTH_001", "유효하지 않은 OAuth 토큰입니다."),
    UNSUPPORTED_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH_002", "지원하지 않는 OAuth 제공자입니다."),
    OAUTH_EMAIL_CONFLICT(HttpStatus.CONFLICT, "AUTH_003", "이미 다른 제공자로 가입된 이메일입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_002", "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "유효하지 않은 토큰입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_004", "접근 권한이 없습니다."),
    INVALID_MAGIC_LINK_TARGET(HttpStatus.BAD_REQUEST, "AUTH_010", "유효하지 않은 이메일/전화번호 형식입니다."),
    MAGIC_LINK_RATE_LIMIT(HttpStatus.TOO_MANY_REQUESTS, "AUTH_011", "매직 링크 발급 횟수를 초과했습니다. (분당 3회)"),
    MAGIC_LINK_EXPIRED(HttpStatus.BAD_REQUEST, "AUTH_020", "만료된 매직 링크입니다."),
    MAGIC_LINK_BIRTH_DATE_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH_021", "2차 인증(생년월일)이 일치하지 않습니다."),
    MAGIC_LINK_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_022", "존재하지 않는 매직 링크 토큰입니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_030", "만료된 Refresh Token입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_031", "유효하지 않은 Refresh Token입니다."),
    CONSENT_REQUIRED_FIELDS(HttpStatus.BAD_REQUEST, "AUTH_040", "필수 동의 항목이 체크되지 않았습니다."),
    ROLE_NOT_FOUND(HttpStatus.BAD_REQUEST, "AUTH_051", "존재하지 않는 역할 ID입니다."),
    CANNOT_CHANGE_OWN_ROLE(HttpStatus.FORBIDDEN, "AUTH_052", "자기 자신의 역할은 변경할 수 없습니다."),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_001", "회원을 찾을 수 없습니다."),

    // Patient
    PATIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PATIENT_001", "환자를 찾을 수 없습니다."),
    PASSPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "PATIENT_002", "여권 정보를 찾을 수 없습니다."),
    PASSPORT_ALREADY_EXISTS(HttpStatus.CONFLICT, "PATIENT_003", "여권 정보가 이미 존재합니다."),
    QUESTIONNAIRE_NOT_FOUND(HttpStatus.NOT_FOUND, "PATIENT_004", "문진표를 찾을 수 없습니다."),
    QUESTIONNAIRE_ALREADY_EXISTS(HttpStatus.CONFLICT, "PATIENT_005", "문진표가 이미 존재합니다."),
    EMERGENCY_CONTACT_NOT_FOUND(HttpStatus.NOT_FOUND, "PATIENT_006", "긴급 연락처를 찾을 수 없습니다."),
    PATIENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PATIENT_007", "본인의 데이터만 접근할 수 있습니다."),

    // Staff
    STAFF_NOT_FOUND(HttpStatus.NOT_FOUND, "STAFF_001", "실무자를 찾을 수 없습니다."),
    STAFF_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "STAFF_002", "실무자 프로필을 찾을 수 없습니다."),

    // Agency
    AGENCY_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "AGENCY_001", "에이전시 프로필을 찾을 수 없습니다."),

    // Journey
    JOURNEY_NOT_FOUND(HttpStatus.NOT_FOUND, "JOURNEY_001", "여정을 찾을 수 없습니다."),

    // Proposal
    PROPOSAL_NOT_FOUND(HttpStatus.NOT_FOUND, "PROPOSAL_001", "견적서를 찾을 수 없습니다."),
    PROPOSAL_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PROPOSAL_002", "본인의 견적서만 접근할 수 있습니다."),
    PROPOSAL_INVALID_STATUS(HttpStatus.BAD_REQUEST, "PROPOSAL_003", "유효하지 않은 견적서 상태 전이입니다."),
    PROPOSAL_ALREADY_RESPONDED(HttpStatus.BAD_REQUEST, "PROPOSAL_004", "이미 응답한 견적서입니다."),

    // Chat
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_001", "채팅방을 찾을 수 없습니다."),

    // File
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FIL_000", "파일 업로드에 실패했습니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "FIL_001", "파일 크기가 20MB를 초과했습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "FIL_002", "지원하지 않는 파일 형식입니다."),
    INVALID_FILE_CATEGORY(HttpStatus.BAD_REQUEST, "FIL_003", "유효하지 않은 파일 카테고리입니다."),
    FILE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "FIL_010", "파일 접근 권한이 없습니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FIL_011", "존재하지 않는 파일입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
