CREATE TABLE api_clients (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_name        VARCHAR(120) NOT NULL,
    api_key_hash       VARCHAR(64) NOT NULL,
    active             BOOLEAN NOT NULL DEFAULT TRUE,
    requests_per_minute INTEGER NOT NULL DEFAULT 60,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_api_clients_key_hash UNIQUE (api_key_hash)
);

-- Cliente de sandbox para desenvolvimento/demonstração local.
-- Chave em texto claro (uso local apenas): gwb2_sandbox_2ae1b72882db4b87c712424149ef8524
-- SHA-256 dessa chave é o valor abaixo - a chave em texto claro NUNCA é armazenada.
INSERT INTO api_clients (client_name, api_key_hash, active, requests_per_minute)
VALUES ('sandbox', '613802a4e057cd16fa87036066b406ade4e00738cb8d386e54223ebc98973c39', TRUE, 120);
