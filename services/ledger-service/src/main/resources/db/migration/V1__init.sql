CREATE TABLE ledger_accounts (
    id                  UUID PRIMARY KEY,
    kind                VARCHAR(30) NOT NULL,
    code                VARCHAR(100) NOT NULL,
    name                VARCHAR(180) NOT NULL,
    external_account_id UUID,
    currency            VARCHAR(3) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_ledger_accounts_code UNIQUE (code),
    CONSTRAINT uk_ledger_accounts_external UNIQUE (external_account_id)
);

CREATE TABLE journal_entries (
    id              UUID PRIMARY KEY,
    description     VARCHAR(255) NOT NULL,
    idempotency_key VARCHAR(180) NOT NULL,
    source_event    VARCHAR(60),
    created_at      TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_journal_entries_idempotency UNIQUE (idempotency_key)
);

CREATE TABLE postings (
    id                UUID PRIMARY KEY,
    journal_entry_id  UUID NOT NULL REFERENCES journal_entries (id),
    ledger_account_id UUID NOT NULL REFERENCES ledger_accounts (id),
    entry_type        VARCHAR(10) NOT NULL,
    amount            NUMERIC(19, 2) NOT NULL,
    currency          VARCHAR(3) NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_postings_ledger_account ON postings (ledger_account_id);
CREATE INDEX idx_postings_journal_entry ON postings (journal_entry_id);
