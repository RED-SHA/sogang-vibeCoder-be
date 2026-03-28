-- V3: Sprint 2 - Patient Onboarding (PAT-201, PAT-202, PAT-203)
-- PatientPassport, MedicalQuestionnaire, EmergencyContact

-- 여권 정보
CREATE TABLE patient_passport (
    id                    BIGSERIAL    PRIMARY KEY,
    member_id             BIGINT       NOT NULL REFERENCES member(id),
    passport_number       VARCHAR(20)  NOT NULL,
    full_name             VARCHAR(200) NOT NULL,
    nationality           VARCHAR(100) NOT NULL,
    birth_date            DATE         NOT NULL,
    expiry_date           DATE         NOT NULL,
    gender                VARCHAR(10)  NOT NULL,
    input_type            VARCHAR(10)  NOT NULL,
    file_id               BIGINT,
    ocr_confidence        DOUBLE PRECISION,
    verification_status   VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    verified_at           TIMESTAMP,
    created_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at            TIMESTAMP
);

CREATE INDEX idx_patient_passport_member_id ON patient_passport(member_id);

-- 의료 문진표
CREATE TABLE medical_questionnaire (
    id                    BIGSERIAL    PRIMARY KEY,
    member_id             BIGINT       NOT NULL REFERENCES member(id),
    blood_type            VARCHAR(20)  NOT NULL,
    height                DOUBLE PRECISION,
    weight                DOUBLE PRECISION,
    allergies             JSONB,
    current_medications   JSONB,
    past_surgeries        JSONB,
    chronic_conditions    JSONB,
    additional_notes      TEXT,
    status                VARCHAR(20)  NOT NULL DEFAULT 'SUBMITTED',
    created_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at            TIMESTAMP
);

CREATE INDEX idx_medical_questionnaire_member_id ON medical_questionnaire(member_id);

-- 긴급 연락처
CREATE TABLE emergency_contact (
    id                    BIGSERIAL    PRIMARY KEY,
    member_id             BIGINT       NOT NULL REFERENCES member(id),
    name                  VARCHAR(100) NOT NULL,
    relationship          VARCHAR(50)  NOT NULL,
    phone                 VARCHAR(30)  NOT NULL,
    email                 VARCHAR(255),
    is_primary            BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at            TIMESTAMP
);

CREATE INDEX idx_emergency_contact_member_id ON emergency_contact(member_id);
