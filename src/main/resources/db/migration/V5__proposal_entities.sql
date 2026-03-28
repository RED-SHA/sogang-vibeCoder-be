-- Proposal (견적서 마스터)
CREATE TABLE proposal (
    id              BIGSERIAL       PRIMARY KEY,
    patient_id      BIGINT          NOT NULL REFERENCES member(id),
    title           VARCHAR(200)    NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'DRAFT',
    currency        VARCHAR(10)     NOT NULL DEFAULT 'USD',
    subtotal        NUMERIC(15, 2)  DEFAULT 0,
    discount_rate   NUMERIC(5, 2)   DEFAULT 0,
    discount_amount NUMERIC(15, 2)  DEFAULT 0,
    total_amount    NUMERIC(15, 2)  DEFAULT 0,
    valid_until     TIMESTAMP,
    notes           TEXT,
    sent_at         TIMESTAMP,
    responded_at    TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP
);

CREATE INDEX idx_proposal_patient_id ON proposal(patient_id);
CREATE INDEX idx_proposal_status ON proposal(status);

-- Proposal Item (견적 항목)
CREATE TABLE proposal_item (
    id          BIGSERIAL       PRIMARY KEY,
    proposal_id BIGINT          NOT NULL REFERENCES proposal(id) ON DELETE CASCADE,
    category    VARCHAR(30)     NOT NULL,
    name        VARCHAR(200)    NOT NULL,
    description TEXT,
    unit_price  NUMERIC(15, 2)  NOT NULL,
    quantity    INT             NOT NULL DEFAULT 1,
    amount      NUMERIC(15, 2)  NOT NULL,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMP
);

CREATE INDEX idx_proposal_item_proposal_id ON proposal_item(proposal_id);

-- Proposal Request (환자 견적 요청)
CREATE TABLE proposal_request (
    id                        BIGSERIAL       PRIMARY KEY,
    patient_id                BIGINT          NOT NULL REFERENCES member(id),
    desired_procedures        JSONB,
    preferred_hospital_ids    JSONB,
    arrival_date              DATE,
    departure_date            DATE,
    accommodation_preference  VARCHAR(50),
    concierge_services        JSONB,
    budget_min                NUMERIC(15, 2),
    budget_max                NUMERIC(15, 2),
    budget_currency           VARCHAR(10),
    additional_requests       TEXT,
    status                    VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    created_at                TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at                TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted_at                TIMESTAMP
);

CREATE INDEX idx_proposal_request_patient_id ON proposal_request(patient_id);
CREATE INDEX idx_proposal_request_status ON proposal_request(status);
