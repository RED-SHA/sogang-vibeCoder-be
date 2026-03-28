-- Journey Template (여정 템플릿)
CREATE TABLE journey_template (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(200)    NOT NULL UNIQUE,
    category        VARCHAR(20)     NOT NULL,
    duration_days   INT             NOT NULL,
    usage_count     INT             NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP
);

-- Journey Template Item (템플릿 항목)
CREATE TABLE journey_template_item (
    id              BIGSERIAL       PRIMARY KEY,
    template_id     BIGINT          NOT NULL REFERENCES journey_template(id) ON DELETE CASCADE,
    day_offset      INT             NOT NULL,
    time_offset     VARCHAR(5)      NOT NULL,
    title           VARCHAR(200)    NOT NULL,
    type            VARCHAR(20)     NOT NULL,
    description     TEXT,
    duration_minutes INT,
    location        JSONB,
    required_staff  JSONB,
    sort_order      INT             NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP
);

CREATE INDEX idx_template_item_template_id ON journey_template_item(template_id);

-- Journey (환자 여정)
CREATE TABLE journey (
    id              BIGSERIAL       PRIMARY KEY,
    patient_id      BIGINT          NOT NULL REFERENCES member(id),
    title           VARCHAR(200)    NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PLANNED',
    start_date      DATE            NOT NULL,
    end_date        DATE            NOT NULL,
    notes           TEXT,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP
);

CREATE INDEX idx_journey_patient_id ON journey(patient_id);
CREATE INDEX idx_journey_status ON journey(status);
CREATE INDEX idx_journey_start_date ON journey(start_date);

-- Journey Schedule Item (여정 일정 항목)
CREATE TABLE journey_schedule_item (
    id              BIGSERIAL       PRIMARY KEY,
    journey_id      BIGINT          NOT NULL REFERENCES journey(id) ON DELETE CASCADE,
    day_number      INT             NOT NULL,
    scheduled_at    TIMESTAMP       NOT NULL,
    title           VARCHAR(200)    NOT NULL,
    type            VARCHAR(20)     NOT NULL,
    description     TEXT,
    status          VARCHAR(20)     NOT NULL DEFAULT 'SCHEDULED',
    duration_minutes INT,
    location        JSONB,
    completed_at    TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP
);

CREATE INDEX idx_schedule_item_journey_id ON journey_schedule_item(journey_id);
CREATE INDEX idx_schedule_item_scheduled_at ON journey_schedule_item(scheduled_at);

-- Staff Assignment (실무자 배정)
CREATE TABLE staff_assignment (
    id              BIGSERIAL       PRIMARY KEY,
    schedule_item_id BIGINT         NOT NULL REFERENCES journey_schedule_item(id) ON DELETE CASCADE,
    staff_id        BIGINT          NOT NULL REFERENCES member(id),
    status          VARCHAR(20)     NOT NULL DEFAULT 'ASSIGNED',
    assigned_at     TIMESTAMP       NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP
);

CREATE INDEX idx_staff_assignment_schedule_item_id ON staff_assignment(schedule_item_id);
CREATE INDEX idx_staff_assignment_staff_id ON staff_assignment(staff_id);
