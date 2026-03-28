-- V2: Sprint 1 - Auth 관련 추가 테이블
-- MemberConsent, MagicLink, RefreshToken, StaffProfile, AgencyProfile

-- 약관 동의 이력
CREATE TABLE member_consent (
    id                   BIGSERIAL    PRIMARY KEY,
    member_id            BIGINT       NOT NULL REFERENCES member(id),
    terms_of_service     BOOLEAN      NOT NULL DEFAULT FALSE,
    privacy_policy       BOOLEAN      NOT NULL DEFAULT FALSE,
    medical_data_consent BOOLEAN      NOT NULL DEFAULT FALSE,
    marketing_consent    BOOLEAN      NOT NULL DEFAULT FALSE,
    consent_version      VARCHAR(20)  NOT NULL,
    consented_at         TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at           TIMESTAMP
);

CREATE INDEX idx_member_consent_member_id ON member_consent(member_id);

-- 매직 링크 토큰
CREATE TABLE magic_link (
    id            BIGSERIAL    PRIMARY KEY,
    token         UUID         NOT NULL UNIQUE,
    target_email  VARCHAR(255),
    target_phone  VARCHAR(30),
    target_type   VARCHAR(10)  NOT NULL,
    role          VARCHAR(50)  NOT NULL,
    language      VARCHAR(5)   NOT NULL DEFAULT 'en',
    birth_date    DATE,
    expires_at    TIMESTAMP    NOT NULL,
    used_at       TIMESTAMP,
    member_id     BIGINT       REFERENCES member(id),
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at    TIMESTAMP
);

CREATE INDEX idx_magic_link_token ON magic_link(token);
CREATE INDEX idx_magic_link_target_email ON magic_link(target_email);
CREATE INDEX idx_magic_link_expires_at ON magic_link(expires_at);

-- 리프레시 토큰
CREATE TABLE refresh_token (
    id          BIGSERIAL    PRIMARY KEY,
    token       VARCHAR(500) NOT NULL UNIQUE,
    member_id   BIGINT       NOT NULL REFERENCES member(id),
    expires_at  TIMESTAMP    NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMP
);

CREATE INDEX idx_refresh_token_token ON refresh_token(token);
CREATE INDEX idx_refresh_token_member_id ON refresh_token(member_id);

-- 실무자 프로필
CREATE TABLE staff_profile (
    id           BIGSERIAL    PRIMARY KEY,
    member_id    BIGINT       NOT NULL UNIQUE REFERENCES member(id),
    staff_type   VARCHAR(20)  NOT NULL,
    languages    JSONB,
    vehicle_info JSONB,
    is_available BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at   TIMESTAMP
);

CREATE INDEX idx_staff_profile_member_id ON staff_profile(member_id);
CREATE INDEX idx_staff_profile_staff_type ON staff_profile(staff_type);

-- 에이전시/병원 프로필
CREATE TABLE agency_profile (
    id               BIGSERIAL    PRIMARY KEY,
    name             VARCHAR(200) NOT NULL,
    license_number   VARCHAR(100) NOT NULL UNIQUE,
    license_verified BOOLEAN      NOT NULL DEFAULT FALSE,
    address          VARCHAR(500),
    phone            VARCHAR(30),
    website          VARCHAR(500),
    description      TEXT,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at       TIMESTAMP
);

CREATE INDEX idx_agency_profile_license_number ON agency_profile(license_number);
