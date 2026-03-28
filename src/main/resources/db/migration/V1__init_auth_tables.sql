-- V1: Auth & RBAC 기초 테이블
-- Role-Permission N:M 관계 (MVP: Role 기반, 추후 Permission 전환 가능)

CREATE TABLE role (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE permission (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE role_permission (
    role_id       BIGINT NOT NULL REFERENCES role(id),
    permission_id BIGINT NOT NULL REFERENCES permission(id),
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE member (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    name            VARCHAR(100) NOT NULL,
    role_id         BIGINT       NOT NULL REFERENCES role(id),
    oauth_provider  VARCHAR(20),
    oauth_id        VARCHAR(255),
    phone           VARCHAR(30),
    language        VARCHAR(5)   NOT NULL DEFAULT 'en',
    profile_image   VARCHAR(500),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP
);

CREATE INDEX idx_member_email ON member(email);
CREATE INDEX idx_member_role_id ON member(role_id);
CREATE INDEX idx_member_oauth ON member(oauth_provider, oauth_id);

-- 초기 역할 데이터
INSERT INTO role (name, description) VALUES
    ('MASTER', '최고 관리자 - 모든 권한'),
    ('ADMIN', '관리자 - 운영 권한'),
    ('PATIENT', '환자 - 본인 데이터 접근'),
    ('STAFF', '실무자 - 배정된 여정 접근');

-- 초기 권한 데이터
INSERT INTO permission (name, description) VALUES
    ('DASHBOARD_VIEW', '대시보드 조회'),
    ('PATIENT_MANAGE', '환자 관리'),
    ('STAFF_ASSIGN', '실무자 배정'),
    ('JOURNEY_MANAGE', '여정 관리'),
    ('CHAT_MONITOR', '채팅 관제'),
    ('PROPOSAL_MANAGE', '견적서 관리'),
    ('NOTIFICATION_SEND', '알림 발송'),
    ('FILE_MANAGE', '파일 관리'),
    ('MEMBER_MANAGE', '회원 관리'),
    ('AFTERCARE_MANAGE', '사후 관리');

-- MASTER: 모든 권한
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id FROM role r, permission p WHERE r.name = 'MASTER';

-- ADMIN: 운영 권한
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id FROM role r, permission p
WHERE r.name = 'ADMIN'
  AND p.name IN ('DASHBOARD_VIEW', 'PATIENT_MANAGE', 'STAFF_ASSIGN', 'JOURNEY_MANAGE',
                  'CHAT_MONITOR', 'PROPOSAL_MANAGE', 'NOTIFICATION_SEND', 'FILE_MANAGE',
                  'AFTERCARE_MANAGE');

-- STAFF: 최소 권한
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id FROM role r, permission p
WHERE r.name = 'STAFF'
  AND p.name IN ('JOURNEY_MANAGE', 'FILE_MANAGE');

-- PATIENT: 본인 데이터 접근 (Service 레이어에서 검증)
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id FROM role r, permission p
WHERE r.name = 'PATIENT'
  AND p.name IN ('FILE_MANAGE');
