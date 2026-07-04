#!/bin/bash
# Cria um banco de dados separado por microsserviço (padrão "database per service").
# Executado automaticamente pela imagem oficial do Postgres via docker-entrypoint-initdb.d.
set -euo pipefail

DATABASES=(kyc_db account_db ledger_db payment_db card_db gateway_db)

for db in "${DATABASES[@]}"; do
  echo "Criando banco de dados: $db"
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    SELECT 'CREATE DATABASE $db'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$db')\gexec
EOSQL
done
