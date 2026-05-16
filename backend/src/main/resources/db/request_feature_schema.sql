-- ================================================================
-- Request Feature Schema – tables for request lifecycle
-- ================================================================

-- Requests table
CREATE TABLE IF NOT EXISTS requests (
    id               BIGSERIAL    PRIMARY KEY,
    requester_id     BIGINT       NOT NULL REFERENCES users(id),
    status           VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    remarks          TEXT,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Request items table
CREATE TABLE IF NOT EXISTS request_items (
    id                 BIGSERIAL   PRIMARY KEY,
    request_id         BIGINT      NOT NULL REFERENCES requests(id) ON DELETE CASCADE,
    inventory_item_id  BIGINT      NOT NULL REFERENCES inventory_items(id),
    quantity           INTEGER     NOT NULL,
    unit_snapshot      VARCHAR(20),
    expiration_snapshot DATE,
    created_at         TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- Transaction ledger (immutable)
CREATE TABLE IF NOT EXISTS transactions (
    id                 BIGSERIAL   PRIMARY KEY,
    request_id         BIGINT      REFERENCES requests(id),
    inventory_item_id  BIGINT      NOT NULL REFERENCES inventory_items(id),
    change_type        VARCHAR(50) NOT NULL,
    quantity_change    INTEGER     NOT NULL,
    balance_after      INTEGER     NOT NULL,
    actor_id           BIGINT      NOT NULL REFERENCES users(id),
    remarks            TEXT,
    created_at         TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- Uploaded files table (SDS)
CREATE TABLE IF NOT EXISTS uploaded_files (
    id                 BIGSERIAL   PRIMARY KEY,
    inventory_item_id  BIGINT      NOT NULL REFERENCES inventory_items(id),
    uploader_id        BIGINT      NOT NULL REFERENCES users(id),
    file_name          VARCHAR(255) NOT NULL,
    file_type          VARCHAR(50)  NOT NULL,
    file_path          VARCHAR(500) NOT NULL,
    file_size          BIGINT       NOT NULL,
    created_at         TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Refresh tokens table
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id           BIGSERIAL    PRIMARY KEY,
    token        VARCHAR(500) NOT NULL UNIQUE,
    user_id      BIGINT       NOT NULL REFERENCES users(id),
    revoked      BOOLEAN      NOT NULL DEFAULT FALSE,
    expires_at   TIMESTAMP    NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_requests_requester ON requests(requester_id);
CREATE INDEX IF NOT EXISTS idx_requests_status ON requests(status);
CREATE INDEX IF NOT EXISTS idx_request_items_request ON request_items(request_id);
CREATE INDEX IF NOT EXISTS idx_transactions_request ON transactions(request_id);
CREATE INDEX IF NOT EXISTS idx_transactions_item ON transactions(inventory_item_id);
CREATE INDEX IF NOT EXISTS idx_uploaded_files_item ON uploaded_files(inventory_item_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token ON refresh_tokens(token);
