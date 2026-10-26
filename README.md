# Fraud Detection Engine

A modular, production-style fraud detection platform built with **Spring Boot**, **Clean Architecture**, and **Domain-Driven Design (DDD)** principles.
The system evaluates transactions against configurable fraud rules and persists fraud decisions and alerts.

The system evaluates incoming financial transactions against configurable fraud rules and produces:

* Fraud checks (risk score + risk level)
* Fraud alerts when suspicious activity is detected

Rules are configurable via YAML, allowing risk policies to be adjusted **without code changes**.

The project demonstrates how a fraud detection engine can be designed to be:

* Modular
* Event-driven
* Testable
* Extensible for real banking systems
---

# Architecture

This project follows **Clean Architecture / Hexagonal Architecture** principles.

```
                +----------------------+
                |      REST API        |
                |  TransactionController|
                +-----------+----------+
                            |
                            v
                +----------------------+
                |   Application Layer  |
                | ProcessTransactionUseCase
                +-----------+----------+
                            |
                            v
                +----------------------+
                |      Domain Layer    |
                | Fraud Rules Engine   |
                +-----------+----------+
                            |
                            v
                +----------------------+
                |   Infrastructure     |
                | PostgreSQL / Kafka   |
                +----------------------+
```

---

# Project Modules

```
fraud-detection-engine
├── fraud-domain
│   └── Core fraud detection rules and domain models
│
├── fraud-application
│   └── Application use cases and command objects
│
├── fraud-api
│   └── REST controllers and request/response DTOs
│
├── fraud-infrastructure
│   └── Persistence adapters and Kafka messaging
│
├── fraud-boot
│   └── Spring Boot runtime and configuration
│
└── docker-compose.yml
```

---

**fraud-api**
REST controllers and DTOs.

**fraud-boot**
Spring Boot application and configuration.

This structure ensures:

* Domain logic is independent of frameworks
* Infrastructure is replaceable
* The system is testable and maintainable

---

# Fraud Rules

Fraud rules are defined in:

```
fraud-boot/src/main/resources/fraud-rules.yml
```

Example configuration:

```yaml
fraud:
  rules:
    large-amount:
      enabled: true
      threshold: 10000
      score: 50

    velocity:
      enabled: true
      window-seconds: 60
      max-transactions: 5
      score: 30

    country-mismatch:
      enabled: true
      score: 40

    blacklisted-merchant:
      enabled: true
      score: 70
      merchants:
        - shady-store
        - scam-pay
```

Example outcome:

| Rule                 | Score    |
| -------------------- | -------- |
| Large Amount         | 50       |
| Country Mismatch     | 40       |
| Blacklisted Merchant | 70       |
| **Total Risk Score** | **160**  |
| **Risk Level**       | **HIGH** |

### Risk Level & Alert Classification

The engine maps the total risk score to categorical risk levels and alert policies:

| Total Score | Risk Level | Fraud Suspected? | Fraud Alert Stored? | Action / Description |
|:---:|:---:|:---:|:---:|---|
| **0 – 29** | `LOW` | `false` | ❌ No | Legitimate transaction. Recorded in transaction history. |
| **30 – 69** | `MEDIUM` | `false` | ❌ No | Moderately suspicious (e.g. single country mismatch or velocity flag). |
| **≥ 70** | `HIGH` | `true` | ✅ **Yes** | High severity. Persisted to `fraud_alerts` table for review. |

> **Note on Fraud Alerts**: `/fraud-alerts` queries only return transactions where `riskLevel == HIGH` (`fraudSuspected == true`). Normal (`LOW`) and moderately suspicious (`MEDIUM`) transactions are evaluated and saved to `transactions` and `fraud_checks`, but do not generate high-severity alerts.

---

# API Endpoints

### 1. Submit Transaction (Synchronous REST)

```http
POST /transactions
Content-Type: application/json
```

#### High-Risk Example (Score: 160 -> HIGH):
```bash
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "ACC-123",
    "amount": 15000,
    "merchant": "shady-store",
    "country": "US",
    "accountHomeCountry": "ZA"
  }'
```

Response:
```json
{
  "transactionId": "a14fa832-3786-406d-b33a-2277365bc546",
  "riskScore": 160,
  "riskLevel": "HIGH",
  "fraudSuspected": true,
  "evaluatedAt": "2026-10-26T09:59:42.531Z",
  "ruleResults": [
    {"ruleName": "LARGE_AMOUNT", "triggered": true, "score": 50, "reason": "Transaction amount exceeds threshold of 10000"},
    {"ruleName": "VELOCITY", "triggered": false, "score": 0, "reason": "Rule passed"},
    {"ruleName": "COUNTRY_MISMATCH", "triggered": true, "score": 40, "reason": "Transaction country does not match account home country"},
    {"ruleName": "BLACKLISTED_MERCHANT", "triggered": true, "score": 70, "reason": "Merchant is blacklisted"}
  ]
}
```

#### Normal / Low-Risk Example (Score: 0 -> LOW):
```bash
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "ACC-555",
    "amount": 100,
    "merchant": "grocery-store",
    "country": "ZA",
    "accountHomeCountry": "ZA"
  }'
```

#### Medium-Risk Example (Score: 40 -> MEDIUM):
```bash
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "ACC-777",
    "amount": 100,
    "merchant": "grocery-store",
    "country": "US",
    "accountHomeCountry": "ZA"
  }'
```

---

### 2. Get Fraud Check Result

```bash
curl -s http://localhost:8080/transactions/{transactionId}/fraud-check
```

---

### 3. Get Fraud Alerts

Query all generated fraud alerts:
```bash
curl -s http://localhost:8080/fraud-alerts
```

Filter alerts by a specific transaction ID:
```bash
curl -s "http://localhost:8080/fraud-alerts?transactionId={transactionId}"
```

---

### 4. Publish Kafka Transaction Event (Testing Ingestion)

Publish an event onto the Kafka topic `fraud.transactions.v1`:
```bash
curl -X POST http://localhost:8080/kafka-test/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "evt-001",
    "source": "ATM",
    "occurredAt": "2026-10-26T10:00:00Z",
    "transactionRequest": {
      "accountId": "ACC-123",
      "amount": 15000,
      "merchant": "shady-store",
      "country": "US",
      "accountHomeCountry": "ZA"
    }
  }'
```

Response:
```text
Published event 0a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5d
```

---

# Running the Application

### Prerequisites
- **Java 17+**
- **Docker & Docker Compose** (or Docker Desktop)
- **Maven 3.8+** (or bundled wrapper)

---

### Option A: Complete Stack with Docker (Recommended)

Starts the Fraud Engine, PostgreSQL, Kafka, and Grafana LGTM stack together:

```bash
docker compose up --build
```

Services started:

| Service        | Port        | Purpose |
|----------------|-------------|---------|
| Fraud Engine   | 8080        | Spring Boot Application REST API |
| PostgreSQL     | 5432        | Persistent database storage |
| Kafka          | 9092        | Event streaming broker |
| Grafana        | 3000        | Observability UI dashboard |
| OTLP Collector | 4317 / 4318 | OpenTelemetry metrics and traces |

---

### Option B: Local Development with Maven

1. **Start the backing services in the background:**
   ```bash
   docker compose up -d postgres kafka lgtm
   ```

2. **Build and package the application:**
   ```bash
   mvn clean package
   ```

3. **Run the Spring Boot application:**
   ```bash
   mvn --projects fraud-boot spring-boot:run
   ```
   *or run the packaged JAR directly:*
   ```bash
   java -jar fraud-boot/target/fraud-boot-1.0.0.jar
   ```

> **Tip**: If you switch from running locally with Maven to running via `docker compose up --build`, ensure any local process on port 8080 is stopped (`lsof -ti :8080 | xargs kill -9`).

---

# OpenTelemetry

The application includes **OpenTelemetry tracing** to track fraud evaluation across the system.

Trace example:

```
POST /transactions
   ↓
fraud.processTransaction
   ↓
Fraud rule evaluation
   ↓
Persist fraud_check
   ↓
Persist fraud_alert
```

Configuration example:

```yaml
otel:
  service:
    name: fraud-engine
  exporter:
    otlp:
      endpoint: http://localhost:4318
```

---

# Observability

The project includes a **local observability stack** using Docker.

Components:

| Component  | Purpose             |
| ---------- | ------------------- |
| Grafana    | Dashboards          |
| Tempo      | Distributed tracing |
| Prometheus | Metrics collection  |
| Loki       | Logs                |

Grafana UI:

```
http://localhost:3000
```

Default login:

```
admin
admin
```

---

## Prometheus Metrics

Metrics endpoint:

```
http://localhost:8080/actuator/prometheus
```

---

# Testing Strategy

### Domain Tests

Validate fraud rules independently.

Examples:

* VelocityRuleTest
* LargeAmountRuleTest

### Application Tests

Verify use case orchestration with mocked ports.

Example:

* ProcessTransactionUseCaseTest

### Infrastructure Tests

Verify persistence adapters and database queries.

Example:

* TransactionHistoryAdapterIntegrationTest

### API Tests

Run against the full Spring Boot application context.

Location:

```
fraud-boot/src/test
```

---

# Kafka Integration

The fraud detection engine also supports **asynchronous transaction ingestion using Apache Kafka**.

This allows the system to process transactions from multiple banking channels such as:

* ATM
* Branch systems
* Merchant / POS terminals
* Mobile banking
* Payment processors
* Card networks

Kafka enables the system to scale horizontally and process large volumes of transactions in real time.

## Kafka Architecture

```
External Channels
     |
     v
+---------------------+
|     Kafka Topics    |
| fraud.transactions  |
+----------+----------+
           |
           v
+---------------------------+
| TransactionKafkaConsumer  |
+-------------+-------------+
              |
              v
+---------------------------+
| ProcessTransactionUseCase |
+-------------+-------------+
              |
              v
+---------------------------+
| Fraud Detection Engine    |
+-------------+-------------+
              |
              v
+---------------------------+
| PostgreSQL Persistence    |
+---------------------------+
```

Kafka acts as **an additional inbound adapter** while preserving the core application and domain layers.

Both REST and Kafka ingestion paths invoke the same application use case.

---

# Kafka Event Model

Kafka messages contain transaction data and metadata describing the source of the event.

Example Kafka event:

```json
{
  "eventId": "evt-001",
  "source": "ATM",
  "occurredAt": "2026-03-12T10:15:00Z",
  "transaction": {
    "accountId": "ACC-123",
    "amount": 15000,
    "merchant": "shady-store",
    "country": "US",
    "accountHomeCountry": "ZA"
  }
}
```

---

# Kafka Topics

| Topic                    | Description                         |
| ------------------------ | ----------------------------------- |
| `fraud.transactions.v1`  | Incoming transaction events         |
| `fraud.transactions.dlq` | Dead-letter queue for failed events |

---

# Idempotency Handling

Kafka consumers may reprocess messages during retries or partition rebalancing.

To prevent duplicate fraud checks, the system implements **idempotent event processing** using a `processed_event` table.

Each event contains a unique `eventId`.

Processing flow:

```
Kafka Event Received
        |
        v
Check processed_event table
        |
        +-- Already processed → ignore
        |
        +-- Not processed → process transaction
                               |
                               v
                     Save eventId to table
```

---

# Database Schema

PostgreSQL migrations are managed with **Flyway** (`classpath:db/migration`).

### 1. `transactions`
Records every raw transaction processed by the engine:
| Column | Type | Description |
|---|---|---|
| `id` | UUID (PK) | Unique transaction identifier |
| `account_id` | VARCHAR(100) | Source bank account identifier |
| `amount` | NUMERIC(19,2) | Transaction monetary amount |
| `merchant` | VARCHAR(255) | Merchant name / identifier |
| `country` | VARCHAR(20) | Country where transaction occurred |
| `account_home_country` | VARCHAR(20) | Account holder registered country |
| `timestamp` | TIMESTAMP | Transaction timestamp |

### 2. `fraud_checks`
Stores evaluation outcomes and composite scores:
| Column | Type | Description |
|---|---|---|
| `transaction_id` | UUID (PK, FK) | Reference to `transactions.id` |
| `risk_score` | INTEGER | Computed risk score |
| `risk_level` | VARCHAR(20) | `LOW`, `MEDIUM`, or `HIGH` |
| `evaluated_at` | TIMESTAMP | Evaluation timestamp |
| `suspected_fraud` | BOOLEAN | `true` if `risk_level == HIGH` |

### 3. `fraud_alerts`
Stores actionable alerts for high-risk suspected fraud:
| Column | Type | Description |
|---|---|---|
| `id` | UUID (PK) | Unique alert identifier |
| `transaction_id` | UUID (FK) | Reference to `transactions.id` |
| `risk_score` | INTEGER | Final risk score |
| `risk_level` | VARCHAR(20) | Severity level (`HIGH`) |
| `triggered_rules` | VARCHAR(2000) | JSON / comma list of triggered rule names |
| `created_at` | TIMESTAMP | Alert creation timestamp |

### 4. `processed_event`
Ensures idempotent Kafka message consumption:
| Column | Type | Description |
|---|---|---|
| `event_id` | VARCHAR(100) (PK) | Unique Kafka event identifier |
| `source` | VARCHAR(50) | Ingestion channel (e.g. `ATM`, `POS`, `MOBILE`) |
| `processed_at` | TIMESTAMP | Timestamp when event was processed |

---

# Testing Strategy

Kafka integration includes multiple test layers.

### Unit Tests

Test the Kafka consumer logic independently.

Example:

```
TransactionKafkaConsumerTest
```

Validates:

* correct mapping of Kafka messages
* invocation of application use case
* duplicate event handling

---

### Integration Tests

Kafka integration tests use **Embedded Kafka** to simulate a real broker.

Example:

```
TransactionKafkaIntegrationTest
```

Validates:

* Kafka listener wiring
* event consumption
* fraud processing pipeline
* idempotency behavior

---

# Technology Stack

* Java 17
* Spring Boot
* Spring Data JPA
* PostgreSQL
* Flyway
* Docker
* JUnit 5
* MockMvc
* Kafka
* OpenTelemetry
* Micrometer / Prometheus
* Grafana
