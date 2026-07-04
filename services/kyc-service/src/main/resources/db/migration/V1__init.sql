CREATE TABLE customers (
    id              UUID PRIMARY KEY,
    person_type     VARCHAR(20) NOT NULL,
    document        VARCHAR(14) NOT NULL,
    name            VARCHAR(180) NOT NULL,
    email           VARCHAR(180) NOT NULL,
    phone           VARCHAR(20),
    birth_date      DATE,
    status          VARCHAR(20) NOT NULL,
    risk_score      INTEGER,
    rejection_reason VARCHAR(255),
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    version         BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_customers_document UNIQUE (document)
);

CREATE INDEX idx_customers_status ON customers (status);
