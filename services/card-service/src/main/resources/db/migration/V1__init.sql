CREATE TABLE cards (
    id              UUID PRIMARY KEY,
    account_id      UUID NOT NULL,
    card_type       VARCHAR(20) NOT NULL,
    holder_name     VARCHAR(180) NOT NULL,
    pan_masked      VARCHAR(20) NOT NULL,
    pan_encrypted   VARCHAR(255) NOT NULL,
    expiry_month    INTEGER NOT NULL,
    expiry_year     INTEGER NOT NULL,
    status          VARCHAR(20) NOT NULL,
    daily_limit     NUMERIC(19, 2) NOT NULL,
    spent_today     NUMERIC(19, 2) NOT NULL DEFAULT 0,
    last_spend_date DATE,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    version         BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_cards_account ON cards (account_id);

CREATE TABLE card_transactions (
    id              UUID PRIMARY KEY,
    card_id         UUID NOT NULL REFERENCES cards (id),
    account_id      UUID NOT NULL,
    merchant_name   VARCHAR(180) NOT NULL,
    amount          NUMERIC(19, 2) NOT NULL,
    currency        VARCHAR(3) NOT NULL,
    status          VARCHAR(20) NOT NULL,
    decline_reason  VARCHAR(255),
    idempotency_key VARCHAR(180) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_card_tx_idempotency UNIQUE (idempotency_key)
);

CREATE INDEX idx_card_tx_card ON card_transactions (card_id);
