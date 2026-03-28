-- ========================================
-- V8: Notification + Aftercare entities
-- Sprint 7
-- ========================================

-- Notification
CREATE TABLE notification (
    id              BIGSERIAL PRIMARY KEY,
    member_id       BIGINT       NOT NULL REFERENCES member(id),
    type            VARCHAR(30)  NOT NULL,
    title           VARCHAR(200) NOT NULL,
    content         TEXT,
    reference_id    BIGINT,
    reference_type  VARCHAR(50),
    is_read         BOOLEAN      NOT NULL DEFAULT FALSE,
    read_at         TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP
);

CREATE INDEX idx_notification_member_id ON notification(member_id);
CREATE INDEX idx_notification_type ON notification(type);
CREATE INDEX idx_notification_is_read ON notification(is_read);

-- Aftercare Guide
CREATE TABLE aftercare_guide (
    id              BIGSERIAL PRIMARY KEY,
    journey_id      BIGINT       NOT NULL REFERENCES journey(id),
    title           VARCHAR(200) NOT NULL,
    content         TEXT,
    instructions    JSONB,
    published_at    TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP
);

CREATE INDEX idx_aftercare_guide_journey_id ON aftercare_guide(journey_id);

-- Invoice
CREATE TABLE invoice (
    id              BIGSERIAL PRIMARY KEY,
    journey_id      BIGINT         NOT NULL REFERENCES journey(id),
    patient_id      BIGINT         NOT NULL REFERENCES member(id),
    invoice_number  VARCHAR(50)    NOT NULL UNIQUE,
    currency        VARCHAR(10)    NOT NULL,
    subtotal        NUMERIC(15,2)  NOT NULL,
    tax             NUMERIC(15,2)  NOT NULL,
    total_amount    NUMERIC(15,2)  NOT NULL,
    status          VARCHAR(20)    NOT NULL DEFAULT 'DRAFT',
    issued_at       TIMESTAMP,
    due_date        DATE,
    created_at      TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP      NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP
);

CREATE INDEX idx_invoice_journey_id ON invoice(journey_id);
CREATE INDEX idx_invoice_patient_id ON invoice(patient_id);
CREATE INDEX idx_invoice_invoice_number ON invoice(invoice_number);

-- Invoice Item
CREATE TABLE invoice_item (
    id              BIGSERIAL PRIMARY KEY,
    invoice_id      BIGINT         NOT NULL REFERENCES invoice(id),
    description     VARCHAR(500)   NOT NULL,
    unit_price      NUMERIC(15,2)  NOT NULL,
    quantity        INTEGER        NOT NULL,
    amount          NUMERIC(15,2)  NOT NULL,
    created_at      TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP      NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP
);

CREATE INDEX idx_invoice_item_invoice_id ON invoice_item(invoice_id);

-- Staff Report
CREATE TABLE staff_report (
    id              BIGSERIAL PRIMARY KEY,
    journey_id      BIGINT       NOT NULL REFERENCES journey(id),
    staff_id        BIGINT       NOT NULL REFERENCES member(id),
    report_content  TEXT         NOT NULL,
    work_hours      DOUBLE PRECISION NOT NULL,
    completed_at    TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP
);

CREATE INDEX idx_staff_report_journey_id ON staff_report(journey_id);
CREATE INDEX idx_staff_report_staff_id ON staff_report(staff_id);
