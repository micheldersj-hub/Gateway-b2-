CREATE TABLE payments (
    id                      UUID PRIMARY KEY,
    account_id              UUID NOT NULL,
    method                  VARCHAR(20) NOT NULL,
    direction               VARCHAR(20) NOT NULL,
    amount                  NUMERIC(19, 2) NOT NULL,
    currency                VARCHAR(3) NOT NULL,
    counterparty_name       VARCHAR(180) NOT NULL,
    counterparty_document   VARCHAR(14),
    counterparty_identifier VARCHAR(180),
    status                  VARCHAR(20) NOT NULL,
    idempotency_key         VARCHAR(180) NOT NULL,
    failure_reason          VARCHAR(255),
    created_at              TIMESTAMPTZ NOT NULL,
    updated_at              TIMESTAMPTZ NOT NULL,
    version                 BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_payments_idempotency UNIQUE (idempotency_key)
);

CREATE INDEX idx_payments_account ON payments (account_id);
CREATE INDEX idx_payments_status ON payments (status);
