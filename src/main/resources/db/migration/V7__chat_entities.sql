-- Chat Room
CREATE TABLE chat_room (
    room_id       VARCHAR(200)  PRIMARY KEY,
    type          VARCHAR(30)   NOT NULL,
    journey_id    BIGINT,
    language      VARCHAR(10),
    created_at    TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE INDEX idx_chat_room_journey ON chat_room (journey_id);
CREATE INDEX idx_chat_room_type ON chat_room (type);

-- Chat Room Participant
CREATE TABLE chat_room_participant (
    id                    BIGSERIAL     PRIMARY KEY,
    room_id               VARCHAR(200)  NOT NULL REFERENCES chat_room (room_id),
    member_id             BIGINT        NOT NULL REFERENCES member (id),
    last_read_message_id  VARCHAR(100),
    joined_at             TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE INDEX idx_chat_participant_room ON chat_room_participant (room_id);
CREATE INDEX idx_chat_participant_member ON chat_room_participant (member_id);
CREATE UNIQUE INDEX uq_chat_participant ON chat_room_participant (room_id, member_id);

-- Chat Message
CREATE TABLE chat_message (
    id                  VARCHAR(100)  PRIMARY KEY,
    room_id             VARCHAR(200)  NOT NULL REFERENCES chat_room (room_id),
    sender_id           BIGINT        NOT NULL,
    sender_name         VARCHAR(100)  NOT NULL,
    sender_role         VARCHAR(30)   NOT NULL,
    type                VARCHAR(20)   NOT NULL,
    content             TEXT,
    translated_content  JSONB         DEFAULT '{}'::jsonb,
    file_id             BIGINT,
    caption             VARCHAR(500),
    is_secure           BOOLEAN       DEFAULT false,
    sent_at             TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE INDEX idx_chat_message_room_sent ON chat_message (room_id, sent_at DESC);
CREATE INDEX idx_chat_message_sender ON chat_message (sender_id);
