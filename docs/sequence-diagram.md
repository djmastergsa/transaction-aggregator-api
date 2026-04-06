# Transaction Aggregator API — Sequence Diagrams

---

## 1. Application Startup & Data Sync

```mermaid
sequenceDiagram
    actor SpringBoot as Spring Boot Context
    participant DSI as DataSourceInitializer
    participant CustRepo as CustomerRepository
    participant AggSvc as TransactionAggregatorService
    participant BankAdapter as BankDataSourceAdapter
    participant CCAdapter as CreditCardDataSourceAdapter
    participant MPAdapter as MobilePaymentDataSourceAdapter
    participant CategSvc as TransactionCategorizationService
    participant TxRepo as TransactionRepository
    participant H2 as H2 Database

    SpringBoot->>DSI: run(ApplicationArguments)
    DSI->>CustRepo: existsByCustomerId(CUST001..CUST005)
    CustRepo->>H2: SELECT count(*) WHERE customer_id = ?
    H2-->>CustRepo: 0 (first run)
    CustRepo-->>DSI: false
    DSI->>CustRepo: save(Customer × 5)
    CustRepo->>H2: INSERT INTO customers (...)
    H2-->>CustRepo: saved
    DSI-->>DSI: seedCustomers() complete

    DSI->>AggSvc: syncAllSources()

    AggSvc->>BankAdapter: fetchTransactions()
    BankAdapter-->>AggSvc: List<RawTransaction> (~250 tx)

    AggSvc->>CCAdapter: fetchTransactions()
    CCAdapter-->>AggSvc: List<RawTransaction> (~150 tx)

    AggSvc->>MPAdapter: fetchTransactions()
    MPAdapter-->>AggSvc: List<RawTransaction> (~100 tx)

    loop For each RawTransaction
        AggSvc->>TxRepo: existsByTransactionRef(ref)
        TxRepo->>H2: SELECT count(*) WHERE transaction_ref = ?
        H2-->>TxRepo: 0 (new)
        TxRepo-->>AggSvc: false

        AggSvc->>CustRepo: findByCustomerId(customerId)
        CustRepo->>H2: SELECT * FROM customers WHERE customer_id = ?
        H2-->>CustRepo: Customer entity
        CustRepo-->>AggSvc: Optional<Customer>

        AggSvc->>AggSvc: new Transaction(raw, customer)
    end

    AggSvc->>TxRepo: saveAllAndFlush(List<Transaction>)
    TxRepo->>H2: INSERT INTO transactions (batch)
    H2-->>TxRepo: saved
    TxRepo-->>AggSvc: persisted count
    AggSvc-->>DSI: total persisted (~500)
```

---

## 2. GET /api/v1/transactions (Filtered & Paginated)

```mermaid
sequenceDiagram
    actor Client
    participant TC as TransactionController
    participant AggSvc as TransactionAggregatorService
    participant TxSpec as TransactionSpecification
    participant TxRepo as TransactionRepository
    participant MapSvc as TransactionMappingService
    participant H2 as H2 Database

    Client->>TC: GET /api/v1/transactions?category=GROCERIES&customerId=CUST001&page=0&size=20&sortBy=transactionDate&sortDirection=DESC

    TC->>TC: build TransactionFilterRequest from @RequestParams

    TC->>AggSvc: getTransactions(filterRequest)

    AggSvc->>TxSpec: buildFilter(filterRequest)
    TxSpec-->>AggSvc: Specification<Transaction> (JPA Criteria)

    AggSvc->>AggSvc: buildSort("transactionDate", "DESC")
    AggSvc->>AggSvc: PageRequest.of(0, 20, sort)

    AggSvc->>TxRepo: findAll(specification, pageable)
    TxRepo->>H2: SELECT * FROM transactions WHERE customer_id=? AND category=? ORDER BY transaction_date DESC LIMIT 20
    H2-->>TxRepo: Page<Transaction>
    TxRepo-->>AggSvc: Page<Transaction>

    loop For each Transaction in page
        AggSvc->>MapSvc: toDto(transaction)
        MapSvc-->>AggSvc: TransactionDto (record)
    end

    AggSvc->>AggSvc: PagedResponse.from(dtoPage)
    AggSvc-->>TC: PagedResponse<TransactionDto>

    TC-->>Client: 200 OK { content: [...], page: 0, size: 20, totalElements: N, ... }
```

---

## 3. GET /api/v1/transactions/aggregate (Aggregation Summary)

```mermaid
sequenceDiagram
    actor Client
    participant TC as TransactionController
    participant AggSvc as TransactionAggregatorService
    participant TxSpec as TransactionSpecification
    participant TxRepo as TransactionRepository
    participant H2 as H2 Database

    Client->>TC: GET /api/v1/transactions/aggregate?dateFrom=2024-01-01&dateTo=2024-12-31

    TC->>AggSvc: getAggregationSummary(filterRequest)

    AggSvc->>TxSpec: buildFilter(filterRequest)
    TxSpec-->>AggSvc: Specification<Transaction>

    AggSvc->>TxRepo: findAll(specification)
    TxRepo->>H2: SELECT * FROM transactions WHERE transaction_date BETWEEN ? AND ?
    H2-->>TxRepo: List<Transaction>
    TxRepo-->>AggSvc: List<Transaction>

    AggSvc->>AggSvc: stream().filter(DEBIT).map(amount).reduce() → totalDebits
    AggSvc->>AggSvc: stream().filter(CREDIT).map(amount).reduce() → totalCredits
    AggSvc->>AggSvc: totalCredits - totalDebits → netPosition
    AggSvc->>AggSvc: groupingBy(category.name, counting()) → transactionsByCategory
    AggSvc->>AggSvc: groupingBy(category.name, summing()) → amountByCategory
    AggSvc->>AggSvc: groupingBy(sourceSystem, counting()) → transactionsBySource
    AggSvc->>AggSvc: groupingBy(type.name, counting()) → transactionsByType
    AggSvc->>AggSvc: min/max(transactionDate) → dateRange

    AggSvc-->>TC: AggregationSummaryDto (record)

    TC-->>Client: 200 OK { totalTransactions, totalDebits, totalCredits, netPosition, transactionsByCategory, ... }
```

---

## 4. GET /api/v1/customers/{customerId}/summary (Customer Financial Summary)

```mermaid
sequenceDiagram
    actor Client
    participant CC as CustomerController
    participant CustSvc as CustomerService
    participant CustRepo as CustomerRepository
    participant AggSvc as TransactionAggregatorService
    participant TxRepo as TransactionRepository
    participant H2 as H2 Database

    Client->>CC: GET /api/v1/customers/CUST001/summary

    CC->>CustSvc: getCustomerSummary("CUST001")

    CustSvc->>CustRepo: findByCustomerId("CUST001")
    CustRepo->>H2: SELECT * FROM customers WHERE customer_id = 'CUST001'
    H2-->>CustRepo: Customer entity
    CustRepo-->>CustSvc: Optional<Customer>

    CustSvc->>AggSvc: getCategorySummary("CUST001")
    AggSvc->>TxRepo: findCategorySummaryForCustomer("CUST001")
    TxRepo->>H2: SELECT category, COUNT(*), SUM(amount) FROM transactions WHERE customer_id = ? GROUP BY category
    H2-->>TxRepo: Object[][]
    TxRepo-->>AggSvc: raw rows
    AggSvc->>AggSvc: compute percentages & averages per category
    AggSvc-->>CustSvc: List<CategorySummaryDto>

    CustSvc->>TxRepo: sumByTypeForCustomer("CUST001", CREDIT)
    TxRepo->>H2: SELECT SUM(amount) WHERE customer_id=? AND type='CREDIT'
    H2-->>TxRepo: BigDecimal
    TxRepo-->>CustSvc: totalIncome

    CustSvc->>TxRepo: sumByTypeForCustomer("CUST001", DEBIT)
    TxRepo->>H2: SELECT SUM(amount) WHERE customer_id=? AND type='DEBIT'
    H2-->>TxRepo: BigDecimal
    TxRepo-->>CustSvc: totalExpenses

    CustSvc->>CustSvc: netPosition = totalIncome - totalExpenses
    CustSvc->>CustSvc: topCategories = sort by totalAmount, take top 3
    CustSvc->>CustSvc: new CustomerSummaryDto(...)

    CustSvc-->>CC: CustomerSummaryDto (record)
    CC-->>Client: 200 OK { customerId, totalIncome, totalExpenses, netPosition, topCategories, ... }
```

---

## 5. POST /api/v1/transactions/sync (Manual Re-sync)

```mermaid
sequenceDiagram
    actor Client
    participant TC as TransactionController
    participant AggSvc as TransactionAggregatorService
    participant BankAdapter as BankDataSourceAdapter
    participant CCAdapter as CreditCardDataSourceAdapter
    participant MPAdapter as MobilePaymentDataSourceAdapter
    participant TxRepo as TransactionRepository
    participant H2 as H2 Database

    Client->>TC: POST /api/v1/transactions/sync

    TC->>AggSvc: syncAllSources()

    par Fetch from all adapters
        AggSvc->>BankAdapter: fetchTransactions()
        BankAdapter-->>AggSvc: List<RawTransaction>
    and
        AggSvc->>CCAdapter: fetchTransactions()
        CCAdapter-->>AggSvc: List<RawTransaction>
    and
        AggSvc->>MPAdapter: fetchTransactions()
        MPAdapter-->>AggSvc: List<RawTransaction>
    end

    loop For each RawTransaction (across all sources)
        AggSvc->>TxRepo: existsByTransactionRef(ref)
        TxRepo->>H2: SELECT count(*) WHERE transaction_ref = ?
        H2-->>TxRepo: count

        alt Already exists (duplicate)
            TxRepo-->>AggSvc: true → skip
        else New transaction
            TxRepo-->>AggSvc: false → add to batch
        end
    end

    AggSvc->>TxRepo: saveAllAndFlush(newTransactions)
    TxRepo->>H2: INSERT INTO transactions (batch)
    H2-->>TxRepo: saved
    TxRepo-->>AggSvc: void

    AggSvc-->>TC: int (newly persisted count)
    TC-->>Client: 200 OK { "message": "Sync complete. 0 new transactions persisted." }
```

---

## 6. Transaction Categorization Flow (Strategy Pattern)

```mermaid
sequenceDiagram
    participant Adapter as DataSourceAdapter
    participant AggSvc as TransactionAggregatorService
    participant CategSvc as TransactionCategorizationService

    Note over Adapter,CategSvc: Called during mock data generation inside each adapter

    Adapter->>CategSvc: categorize(description, merchant)

    CategSvc->>CategSvc: buildSearchText(description, merchant)<br/>→ lowercase concat of both fields

    loop Priority order: SALARY → GROCERIES → UTILITIES → ENTERTAINMENT → DINING → TRANSPORT → HEALTHCARE → SHOPPING → TRANSFER
        CategSvc->>CategSvc: matchesAny(searchText, keywords[category])
        alt keyword found in searchText
            CategSvc-->>Adapter: TransactionCategory (first match wins)
        else no match
            CategSvc->>CategSvc: try next category
        end
    end

    alt No category matched
        CategSvc-->>Adapter: TransactionCategory.OTHER
    end
```
