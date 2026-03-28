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
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_002", "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "유효하지 않은 토큰입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_004", "접근 권한이 없습니다."),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_001", "회원을 찾을 수 없습니다."),

    // Patient
    PATIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PATIENT_001", "환자를 찾을 수 없습니다."),

    // Staff
    STAFF_NOT_FOUND(HttpStatus.NOT_FOUND, "STAFF_001", "실무자를 찾을 수 없습니다."),

    // Journey
    JOURNEY_NOT_FOUND(HttpStatus.NOT_FOUND, "JOURNEY_001", "여정을 찾을 수 없습니다."),

    // Proposal
    PROPOSAL_NOT_FOUND(HttpStatus.NOT_FOUND, "PROPOSAL_001", "견적서를 찾을 수 없습니다."),

    // Chat
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_001", "채팅방을 찾을 수 없습니다."),

    // File
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_001", "파일 업로드에 실패했습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "FILE_002", "허용되지 않은 파일 형식입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
