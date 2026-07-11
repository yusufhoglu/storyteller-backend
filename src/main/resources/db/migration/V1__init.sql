CREATE TABLE users (
    id              UUID PRIMARY KEY,
    firebase_uid    VARCHAR(128) NOT NULL UNIQUE,
    auth_provider   VARCHAR(64)  NOT NULL DEFAULT 'anonymous',
    plan            VARCHAR(32)  NOT NULL DEFAULT 'FREE',
    age             INTEGER,
    gender          VARCHAR(32),
    language        VARCHAR(32),
    fav_books        TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE voice_session (
    id                UUID PRIMARY KEY,
    user_id           UUID NOT NULL REFERENCES users(id),
    room_name         VARCHAR(128) NOT NULL UNIQUE,
    agent_name        VARCHAR(64)  NOT NULL,
    status            VARCHAR(32)  NOT NULL DEFAULT 'CREATED',
    metadata_json     TEXT,
    started_at        TIMESTAMPTZ,
    ended_at          TIMESTAMPTZ,
    duration_seconds  INTEGER,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_voice_sessions_user_id ON voice_session(user_id);
CREATE INDEX idx_voice_sessions_room_name ON voice_session(room_name);