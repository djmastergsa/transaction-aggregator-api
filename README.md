# Transaction Aggregator API

A production-grade Spring Boot 3.2 REST API that aggregates customer financial transaction data
from multiple mock data sources (Bank, Credit Card, Mobile Payment), categorizes each transaction
using a rule-based engine, and exposes an extensive analytics API.

Built as a technical assessment for **Capitec Bank**.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    REST API Layer                           │
│   TransactionController       CustomerController            │
│   /api/v1/transactions        /api/v1/customers             │
└─────────────────┬───────────────────────┬───────────────────┘
                  │                       │
┌─────────────────▼───────────────────────▼───────────────────┐
│                   Service Layer                             │
│  TransactionAggregatorService    CustomerService            │
│  TransactionCategorizationService (Strategy Pattern)        │
└─────────────────┬───────────────────────┬───────────────────┘
                  │                       │
┌─────────────────▼───────────────────────▼───────────────────┐
│              Data Source Adapters (Adapter Pattern)         │
│   BankDataSourceAdapter                                     │
│   CreditCardDataSourceAdapter                               │
│   MobilePaymentDataSourceAdapter                            │
└──────────────────────────────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────────────┐
│                 Repository Layer (Spring Data JPA)          │
│  TransactionRepository    CustomerRepository                │
│  TransactionSpecification (dynamic filtering)               │
└─────────────────────────────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────────────┐
│          Database: H2 (dev) / PostgreSQL (prod)             │
└──────────────────────────────────────────────────────────────┘
```

### Design Patterns

- **Adapter Pattern**: Each data source (`BankDataSourceAdapter`, `CreditCardDataSourceAdapter`,
  `MobilePaymentDataSourceAdapter`) implements the `DataSourceAdapter` interface, adapting the
  source's specific format into a unified `RawTransaction` model.

- **Strategy Pattern**: `TransactionCategorizationService` encapsulates keyword-based categorization
  rules. The implementation (`TransactionCategorizationServiceImpl`) can be swapped without
  changing callers, enabling alternative categorization strategies (e.g. ML-based).

- **Repository + Specification Pattern**: `TransactionSpecification` builds dynamic JPA Criteria
  predicates from a `TransactionFilterRequest`, enabling type-safe dynamic queries without
  string concatenation.

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 21+ |
| Maven | 3.9+ |
| Docker | 24+ (optional) |

---

## Running the Application

### Option 1: Maven (Development)

```bash
# Clone or navigate to the project
cd transaction-aggregator-api

# Build and run (H2 in-memory database)
mvn spring-boot:run

# The API will start on http://localhost:8080
```

### Option 2: JAR (Production-like)

```bash
# Build the JAR
mvn clean package -DskipTests

# Run the JAR
java -jar target/transaction-aggregator-api-1.0.0.jar

# Run with PostgreSQL profile
java -jar target/transaction-aggregator-api-1.0.0.jar --spring.profiles.active=postgres
```

### Option 3: Docker

```bash
# Build the Docker image
docker build -t transaction-aggregator-api:1.0.0 .

# Run the container
docker run -p 8080:8080 --name transaction-api transaction-aggregator-api:1.0.0

# Using docker-compose
docker-compose up --build
```

---

## API Documentation

After starting the application:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs
- **H2 Console** (dev only): http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:transactiondb`
  - Username: `sa` | Password: `password`

---

## Mock Data

On startup, the `DataSourceInitializer` seeds 5 customers and triggers a full data sync from all adapters:

| Source | Transactions/Customer | Focus |
|--------|----------------------|-------|
| BANK | ~50 | Salary, groceries, utilities, transfers |
| CREDIT_CARD | ~30 | Dining, entertainment, shopping |
| MOBILE_PAYMENT | ~20 | Transport (Uber/Bolt), small transfers, dining |

**Total on startup**: ~500 transactions across 5 South African customers (CUST001-CUST005).

---

## API Reference

### Transaction Endpoints (`/api/v1/transactions`)

#### List Transactions with Filters
```bash
# All transactions (paginated)
curl "http://localhost:8080/api/v1/transactions"

# Filter by customer and category
curl "http://localhost:8080/api/v1/transactions?customerId=CUST001&category=GROCERIES"

# Filter by date range and source
curl "http://localhost:8080/api/v1/transactions?sourceSystem=BANK&dateFrom=2024-01-01T00:00:00&dateTo=2024-06-30T23:59:59"

# Filter by amount range with custom sorting
curl "http://localhost:8080/api/v1/transactions?minAmount=1000&maxAmount=50000&sortBy=amount&sortDirection=DESC"

# Custom pagination
curl "http://localhost:8080/api/v1/transactions?page=0&size=50"
```

#### Get Single Transaction
```bash
# By UUID
curl "http://localhost:8080/api/v1/transactions/{uuid}"

# By reference
curl "http://localhost:8080/api/v1/transactions/ref/BANK-CUST001-SAL-001"
```

#### Sync Data Sources
```bash
curl -X POST "http://localhost:8080/api/v1/transactions/sync"
```

#### Aggregation Analytics
```bash
# Overall summary
curl "http://localhost:8080/api/v1/transactions/aggregate"

# Summary filtered to one customer
curl "http://localhost:8080/api/v1/transactions/aggregate?customerId=CUST001"

# Category breakdown
curl "http://localhost:8080/api/v1/transactions/categories/summary"

# Monthly trends (last 12 months)
curl "http://localhost:8080/api/v1/transactions/trends/monthly"

# Per-source summary
curl "http://localhost:8080/api/v1/transactions/sources/summary"
```

### Customer Endpoints (`/api/v1/customers`)

```bash
# List all customers
curl "http://localhost:8080/api/v1/customers"

# Get specific customer
curl "http://localhost:8080/api/v1/customers/CUST001"

# Get customer transactions (paginated + filtered)
curl "http://localhost:8080/api/v1/customers/CUST001/transactions"
curl "http://localhost:8080/api/v1/customers/CUST001/transactions?category=DINING&page=0&size=10"

# Customer financial summary
curl "http://localhost:8080/api/v1/customers/CUST001/summary"

# Customer category breakdown
curl "http://localhost:8080/api/v1/customers/CUST001/categories/summary"

# Customer monthly trends
curl "http://localhost:8080/api/v1/customers/CUST001/trends/monthly"
```

---

## Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=TransactionCategorizationServiceTest
mvn test -Dtest=TransactionAggregatorServiceTest
mvn test -Dtest=TransactionControllerTest
mvn test -Dtest=CustomerControllerTest

# Run tests with coverage report
mvn test jacoco:report
```

### Test Coverage

| Test Class | Tests | Description |
|------------|-------|-------------|
| `TransactionCategorizationServiceTest` | 30+ | Tests every category keyword, edge cases (null/empty/blank), case insensitivity |
| `TransactionAggregatorServiceTest` | 15+ | Mocks adapters and repository; tests sync, deduplication, aggregation |
| `TransactionControllerTest` | 20+ | `@WebMvcTest` for all transaction endpoints with valid/invalid params |
| `CustomerControllerTest` | 20+ | `@WebMvcTest` for all customer endpoints, 404 cases, filter propagation |

---

## Transaction Categories

Categorization is keyword-based (case-insensitive match on description + merchant name):

| Category | Keywords / Merchants |
|----------|---------------------|
| SALARY | salary, payroll, remuneration |
| GROCERIES | woolworths food, pick n pay, checkers, spar, food lovers, freshstop |
| UTILITIES | eskom, city power, telkom, vodacom, mtn, rain, fibre, electricity |
| ENTERTAINMENT | netflix, showmax, dstv, spotify, apple music, ster-kinekor |
| TRANSPORT | uber, bolt, shell, engen, bp, caltex, fuel, gautrain |
| DINING | restaurant, kfc, mcdonalds, steers, wimpy, nando's, spur, ocean basket, coffee, uber eats |
| HEALTHCARE | clicks pharmacy, dischem, hospital, mediclinic, doctor, medical aid |
| SHOPPING | takealot, amazon, mr price, woolworths fashion, h&m, zara, edgars |
| TRANSFER | transfer, eft, payshap, snapscan, home loan, vehicle finance |
| OTHER | anything that doesn't match the above |

---

## Technology Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.2.3 |
| ORM | Spring Data JPA / Hibernate |
| Database (dev) | H2 in-memory |
| Database (prod) | PostgreSQL 15 |
| API Docs | SpringDoc OpenAPI 2.3 (Swagger UI) |
| Boilerplate | Lombok |
| Object Mapping | MapStruct 1.5.5 |
| Testing | JUnit 5 + Mockito + Spring Boot Test |
| Build | Maven 3.9 |
| Container | Docker (multi-stage build) |

---

## Configuration

### application.yml (default - H2)
H2 in-memory database used by default for ease of development and testing.

### application-postgres.yml
Switch to PostgreSQL by running with `--spring.profiles.active=postgres` and ensuring a PostgreSQL instance is available at `localhost:5432/transactiondb`.

### Key Properties
```yaml
app:
  data-sources:
    sync-on-startup: true  # Set to false to disable auto data load
```
