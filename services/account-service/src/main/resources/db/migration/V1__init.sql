CREATE TABLE accounts (
    id              UUID PRIMARY KEY,
    customer_id     UUID NOT NULL,
    person_type     VARCHAR(20) NOT NULL,
    document        VARCHAR(14) NOT NULL,
    holder_name     VARCHAR(180) NOT NULL,
    account_number  VARCHAR(20) NOT NULL,
    branch          VARCHAR(10) NOT NULL,
    account_type    VARCHAR(20) NOT NULL,
    status          VARCHAR(20) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    version         BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_accounts_number UNIQUE (account_number),
    CONSTRAINT uk_accounts_customer UNIQUE (customer_id)
);

CREATE TABLE pix_keys (
    id          UUID PRIMARY KEY,
    account_id  UUID NOT NULL REFERENCES accounts (id),
    key_type    VARCHAR(20) NOT NULL,
    key_value   VARCHAR(180) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_pix_keys_value UNIQUE (key_value)
);

CREATE INDEX idx_pix_keys_account ON pix_keys (account_id);
