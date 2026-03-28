CREATE TABLE file_entity (
    id          BIGSERIAL       PRIMARY KEY,
    uploader_id BIGINT          NOT NULL REFERENCES member(id),
    original_name VARCHAR(500)  NOT NULL,
    stored_name VARCHAR(100)    NOT NULL,
    mime_type   VARCHAR(100)    NOT NULL,
    file_size   BIGINT          NOT NULL,
    category    VARCHAR(30)     NOT NULL,
    s3_key      VARCHAR(500)    NOT NULL,
    url         VARCHAR(1000)   NOT NULL,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMP
);

CREATE INDEX idx_file_entity_uploader_id ON file_entity(uploader_id);
CREATE INDEX idx_file_entity_category ON file_entity(category);
