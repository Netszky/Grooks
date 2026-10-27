CREATE TABLE users
(
    id            UUID PRIMARY KEY DEFAULT uuidv7(),
    email         VARCHAR(255) NOT NULL,
    first_name    VARCHAR(255) NOT NULL,
    last_name     VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT  now(),
    role          VARCHAR(20)  NOT NULL DEFAULT 'MEMBER' CONSTRAINT ck_users_role CHECK (role IN ('MEMBER', 'COACH', 'MANAGER'))
);
CREATE UNIQUE INDEX uq_users_email ON users (lower(email));