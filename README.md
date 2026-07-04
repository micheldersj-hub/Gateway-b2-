# Gateway B2 — Plataforma de Banking as a Service (BaaS)

Plataforma de Banking as a Service em microsserviços, construída em **Java 21 + Spring Boot 3**,
com foco no mercado brasileiro (PIX, TED, Boleto, CPF/CNPJ). Permite que empresas parceiras
ofereçam contas digitais, pagamentos e cartões através de uma API única, sem precisar de uma
licença bancária própria.

## Arquitetura

```
                                   ┌───────────────┐
                     X-API-Key     │   api-gateway  │
        parceiros ───────────────▶│ (WebFlux/Netty)│
                                   │ auth + rate    │
                                   │ limit + CB     │
                                   └───────┬────────┘
                    ┌───────────┬──────────┼───────────┬────────────┐
                    ▼           ▼           ▼           ▼            ▼
              kyc-service  account-   ledger-     payment-      card-service
              (onboarding)  service    service     service      (emissão +
                            (contas    (partidas   (PIX/TED/     autorização)
                             + PIX)     dobradas)   Boleto)
                    │           │           ▲           │            │
                    │           │           │           │            │
                    └────Kafka──┴───────────┴───────────┴────────────┘
                    KycStatusChanged → AccountCreated → PaymentCompleted /
                                        CardTransactionAuthorized → Ledger posta
```

Cada serviço tem seu **próprio banco Postgres** (padrão *database per service*) e se comunica de
forma **assíncrona via Kafka** para propagação de eventos de domínio, e de forma **síncrona via
REST** quando uma resposta imediata é necessária (ex: payment-service consultando saldo no
ledger-service antes de liquidar um pagamento).

### Serviços

| Serviço          | Porta | Responsabilidade                                                        |
|------------------|-------|---------------------------------------------------------------------------|
| `api-gateway`    | 8080  | Roteamento único, autenticação por API Key, rate limiting, circuit breaker |
| `kyc-service`    | 8081  | Onboarding PF/PJ, validação de CPF/CNPJ, score de risco, decisão de compliance |
| `account-service`| 8082  | Contas digitais, chaves PIX, ativação automática pós-KYC                  |
| `ledger-service` | 8083  | Motor de partidas dobradas — **fonte da verdade** para saldos             |
| `payment-service`| 8084  | PIX (envio/recebimento), TED, pagamento de Boleto                        |
| `card-service`   | 8085  | Emissão de cartão virtual/físico, autorização de transações              |

### Por que esse desenho

- **Ledger como fonte única de saldo**: nenhum outro serviço guarda saldo. `account-service`
  guarda metadados da conta; `payment-service`/`card-service` perguntam o saldo disponível ao
  `ledger-service` antes de debitar.
- **Partidas dobradas de verdade**: todo lançamento (`JournalEntry`) tem uma ou mais linhas de
  débito e crédito (`Posting`) que precisam somar zero por moeda — validado em
  `PostingService.validateBalanced`. Lançamentos são imutáveis; correções são feitas com um novo
  lançamento de estorno, nunca com UPDATE/DELETE.
- **Idempotência ponta a ponta**: pagamentos e transações de cartão exigem um header
  `Idempotency-Key`; o ledger também deduplica lançamentos pela mesma chave. Isso protege contra
  reprocessamento de eventos Kafka (at-least-once) e retries de cliente.
- **Consistência eventual controlada**: a criação de conta é reação ao evento
  `KycStatusChangedEvent(APPROVED)`; a criação da conta contábil no ledger é reação a
  `AccountCreatedEvent`. O saldo em si só é alterado de forma síncrona-consultada (ver acima).
- **API Key + rate limiting no gateway**: modelo comum em plataformas BaaS (Stripe e afins) —
  cada parceiro tem uma chave (`X-API-Key`), armazenada apenas como hash SHA-256.

## Stack

- Java 21, Spring Boot 3.3, Spring Cloud Gateway (WebFlux/Netty) 2023.0.3
- PostgreSQL 16 (um banco por serviço) + Flyway para migrações versionadas
- Apache Kafka (KRaft, sem Zookeeper) para eventos de domínio
- Redis para rate limiting distribuído no gateway
- Resilience4j (circuit breaker + rate limiter) via Spring Cloud Circuit Breaker
- springdoc-openapi (Swagger UI) em cada serviço
- Testcontainers/JUnit 5/AssertJ/Mockito para testes

## Rodando localmente

Pré-requisitos: Docker e Docker Compose.

```bash
docker compose up --build
```

Isso sobe Postgres, Kafka (KRaft), Redis e os 6 serviços. As migrações Flyway rodam
automaticamente na subida de cada serviço.

Todas as chamadas passam pelo gateway em `http://localhost:8080`, autenticadas com o header
`X-API-Key`. Um cliente de sandbox já vem semeado via migração:

```
X-API-Key: gwb2_sandbox_2ae1b72882db4b87c712424149ef8524
```

> Essa chave é pública neste repositório propositalmente — é apenas para ambiente local/demo.
> Nunca reutilize esse padrão (chave versionada em texto claro) em produção; lá, gere chaves por
> parceiro e nunca as commite.

Cada serviço também expõe Swagger UI individualmente (útil em desenvolvimento, sem passar pelo
gateway): `http://localhost:808{1..5}/swagger-ui.html`.

## Fluxo de ponta a ponta (exemplo)

```bash
GW=http://localhost:8080
KEY="gwb2_sandbox_2ae1b72882db4b87c712424149ef8524"

# 1. Onboarding do cliente (KYC)
curl -s -X POST $GW/api/v1/customers -H "X-API-Key: $KEY" -H "Content-Type: application/json" -d '{
  "personType": "INDIVIDUAL",
  "document": "52998224725",
  "name": "Maria Silva",
  "email": "maria@example.com",
  "birthDate": "1990-01-01"
}'
# -> se o score de risco for baixo, o cliente já é aprovado automaticamente,
#    o que dispara a criação da conta e da conta contábil no ledger.

# 2. Consultar a conta criada automaticamente
curl -s $GW/api/v1/accounts/by-customer/{customerId} -H "X-API-Key: $KEY"

# 3. Consultar o saldo (zero, recém-criada)
curl -s $GW/api/v1/ledger-accounts/by-external-account/{accountId}/balance -H "X-API-Key: $KEY"

# 4. Simular recebimento de um PIX (webhook do SPI/Bacen)
curl -s -X POST $GW/api/v1/payments/pix/receive -H "X-API-Key: $KEY" \
  -H "Idempotency-Key: pix-recv-001" -H "Content-Type: application/json" -d '{
  "destinationPixKey": "52998224725",
  "amount": 500.00,
  "payerName": "Empresa XYZ",
  "payerDocument": "11222333000181"
}'

# 5. Emitir um cartão virtual
curl -s -X POST $GW/api/v1/cards -H "X-API-Key: $KEY" -H "Content-Type: application/json" -d '{
  "accountId": "{accountId}",
  "cardType": "VIRTUAL",
  "holderName": "MARIA SILVA"
}'

# 6. Autorizar uma compra no cartão
curl -s -X POST $GW/api/v1/cards/{cardId}/authorize -H "X-API-Key: $KEY" \
  -H "Idempotency-Key: card-tx-001" -H "Content-Type: application/json" -d '{
  "amount": 89.90,
  "merchantName": "Mercado ABC"
}'
```

## Segurança e conformidade (o que já existe e o que falta para produção)

Implementado:
- PAN de cartão nunca armazenado em texto claro (AES-256-GCM); CVV nunca persistido.
- Hash (não texto claro) para API Keys de parceiros.
- Validação de CPF/CNPJ por dígito verificador antes de qualquer processamento.
- Ledger imutável e balanceado, com idempotência.

Faltando para produção real (fora do escopo deste MVP, mas necessário antes de operar com dinheiro real):
- Integração real com Bacen (SPI/DICT para PIX, STR para TED, CIP para Boleto) no lugar dos
  simuladores em `payment-service`.
- KMS/HSM para a chave de criptografia de cartão (hoje injetada via variável de ambiente).
- Autenticação forte de parceiro (mTLS ou OAuth2 client-credentials) além da API Key simples.
- Padrão *outbox* / saga para eliminar a janela entre "serviço decide processar" e "evento
  publicado com sucesso" (hoje mitigado por idempotência, mas não é uma garantia transacional).
- Trilha de auditoria completa e retenção de logs conforme regulação do Bacen/LGPD.

## Estrutura do repositório

```
common/                  # DTOs, eventos de domínio, enums e exceções compartilhados
services/
  kyc-service/
  account-service/
  ledger-service/
  payment-service/
  card-service/
  api-gateway/
infra/postgres/          # script de criação dos bancos por serviço
docker-compose.yml
```

## Build e testes

```bash
mvn clean test           # compila e roda os testes de todos os módulos
mvn -pl services/ledger-service -am test   # roda só um módulo (e suas dependências)
```
