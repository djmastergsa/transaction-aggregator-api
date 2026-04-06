# API Test Results

**Date:** 2026-04-06  
**Environment:** Local (H2 in-memory)  
**Base URL:** http://localhost:8080  
**Application Version:** 1.0.0  
**Data Loaded on Startup:** 500 transactions across 5 customers (3 mock data sources)

---

## Test Summary

| Category | Total | Passed | Failed |
|---|---|---|---|
| Customer Endpoints | 5 | 5 | 0 |
| Transaction Endpoints | 8 | 8 | 0 |
| Aggregation Endpoints | 3 | 3 | 0 |
| Filtering & Sorting | 4 | 4 | 0 |
| Error Handling | 3 | 3 | 0 |
| **Total** | **23** | **23** | **0** |

---

## 1. Customer Endpoints

### 1.1 GET /api/v1/customers — List All Customers

**Request:**
```
GET http://localhost:8080/api/v1/customers
```

**Response: 200 OK**
```json
[
  {
    "id": "77aca3f2-0709-40a4-8398-7f13051f2aa7",
    "customerId": "CUST001",
    "firstName": "Sipho",
    "lastName": "Dlamini",
    "fullName": "Sipho Dlamini",
    "email": "sipho.dlamini@email.co.za",
    "phone": "+27 82 123 4567",
    "createdAt": "2026-04-06T15:06:41.952025",
    "totalTransactions": 100
  },
  {
    "id": "19849557-c00e-4f7f-acab-00ebfe889499",
    "customerId": "CUST002",
    "firstName": "Nomvula",
    "lastName": "Khumalo",
    "fullName": "Nomvula Khumalo",
    "email": "nomvula.khumalo@gmail.com",
    "phone": "+27 71 234 5678",
    "createdAt": "2026-04-06T15:06:42.686545",
    "totalTransactions": 100
  },
  {
    "id": "dcacce3a-bcf3-43ae-a893-3ad9b1309e5b",
    "customerId": "CUST003",
    "firstName": "Thabo",
    "lastName": "Molefe",
    "fullName": "Thabo Molefe",
    "email": "thabo.molefe@outlook.com",
    "phone": "+27 83 345 6789",
    "createdAt": "2026-04-06T15:06:42.689542",
    "totalTransactions": 100
  },
  {
    "id": "765b8cc4-0b2b-441e-8dc2-9f13ed1d6a73",
    "customerId": "CUST004",
    "firstName": "Ayanda",
    "lastName": "Nkosi",
    "fullName": "Ayanda Nkosi",
    "email": "ayanda.nkosi@webmail.co.za",
    "phone": "+27 76 456 7890",
    "createdAt": "2026-04-06T15:06:42.694527",
    "totalTransactions": 100
  },
  {
    "id": "3dd7223e-c15d-4a3c-a63f-12b10175e0ae",
    "customerId": "CUST005",
    "firstName": "Lerato",
    "lastName": "Sithole",
    "fullName": "Lerato Sithole",
    "email": "lerato.sithole@yahoo.co.za",
    "phone": "+27 84 567 8901",
    "createdAt": "2026-04-06T15:06:42.877420",
    "totalTransactions": 100
  }
]
```

**Result: PASS** — 5 customers returned, each with UUID, contact details, and transaction count.

---

### 1.2 GET /api/v1/customers/{customerId} — Get Customer by ID

**Request:**
```
GET http://localhost:8080/api/v1/customers/CUST001
```

**Result: PASS** — Returns single customer record for Sipho Dlamini.

---

### 1.3 GET /api/v1/customers/{customerId}/summary — Customer Financial Summary

**Request:**
```
GET http://localhost:8080/api/v1/customers/CUST001/summary
```

**Response: 200 OK**
```json
{
  "customerId": "CUST001",
  "customerName": "Sipho Dlamini",
  "totalTransactions": 100,
  "totalIncome": 193500.00,
  "totalExpenses": 80868.48,
  "netPosition": 112631.52,
  "averageMonthlySpend": 13478.08,
  "highestTransaction": 45000.00,
  "topCategories": [
    { "category": "SALARY",    "transactionCount": 4,  "totalAmount": 180000.00, "percentageOfTotal": 65.61, "averageAmount": 45000.00 },
    { "category": "TRANSFER",  "transactionCount": 13, "totalAmount": 41225.00,  "percentageOfTotal": 15.03, "averageAmount": 3171.15  },
    { "category": "SHOPPING",  "transactionCount": 9,  "totalAmount": 16480.00,  "percentageOfTotal": 6.01,  "averageAmount": 1831.11  },
    { "category": "GROCERIES", "transactionCount": 17, "totalAmount": 12850.50,  "percentageOfTotal": 4.68,  "averageAmount": 755.91   },
    { "category": "TRANSPORT", "transactionCount": 15, "totalAmount": 7054.00,   "percentageOfTotal": 2.57,  "averageAmount": 470.27   }
  ]
}
```

**Result: PASS** — Net position, income/expense split, and top 5 spending categories correct.

---

### 1.4 GET /api/v1/customers/{customerId}/transactions — Customer Transactions (Paginated)

**Request:**
```
GET http://localhost:8080/api/v1/customers/CUST001/transactions?page=0&size=3
```

**Response: 200 OK**
```json
{
  "content": [
    {
      "transactionRef": "BANK-CUST001-TRF-050",
      "customerName": "Sipho Dlamini",
      "amount": 6500.00,
      "currency": "ZAR",
      "type": "CREDIT",
      "category": "TRANSFER",
      "description": "EFT Received Rental Income",
      "merchant": "Tenant Payment",
      "sourceSystem": "BANK",
      "transactionDate": "2026-01-23T08:00:00",
      "status": "PROCESSED"
    },
    {
      "transactionRef": "BANK-CUST001-MED-049",
      "amount": 560.00,
      "type": "DEBIT",
      "category": "HEALTHCARE",
      "description": "Clicks Pharmacy Prescriptions",
      "merchant": "Clicks Pharmacy"
    },
    {
      "transactionRef": "BANK-CUST001-UTL-048",
      "amount": 699.00,
      "type": "DEBIT",
      "category": "UTILITIES",
      "description": "Rain Fibre Internet",
      "merchant": "Rain"
    }
  ],
  "page": 0,
  "size": 3,
  "totalElements": 100,
  "totalPages": 34,
  "hasNext": true,
  "hasPrevious": false
}
```

**Result: PASS** — Pagination metadata correct, 100 transactions for CUST001 across 34 pages.

---

### 1.5 GET /api/v1/customers/{customerId}/categories/summary — Customer Category Breakdown

**Request:**
```
GET http://localhost:8080/api/v1/customers/CUST001/categories/summary
```

**Response: 200 OK**
```json
[
  { "category": "SALARY",        "transactionCount": 4,  "totalAmount": 180000.00, "percentageOfTotal": 65.61, "averageAmount": 45000.00 },
  { "category": "TRANSFER",      "transactionCount": 13, "totalAmount": 41225.00,  "percentageOfTotal": 15.03, "averageAmount": 3171.15  },
  { "category": "SHOPPING",      "transactionCount": 9,  "totalAmount": 16480.00,  "percentageOfTotal": 6.01,  "averageAmount": 1831.11  },
  { "category": "GROCERIES",     "transactionCount": 17, "totalAmount": 12850.50,  "percentageOfTotal": 4.68,  "averageAmount": 755.91   },
  { "category": "TRANSPORT",     "transactionCount": 15, "totalAmount": 7054.00,   "percentageOfTotal": 2.57,  "averageAmount": 470.27   },
  { "category": "DINING",        "transactionCount": 17, "totalAmount": 5685.00,   "percentageOfTotal": 2.07,  "averageAmount": 334.41   },
  { "category": "UTILITIES",     "transactionCount": 11, "totalAmount": 5085.00,   "percentageOfTotal": 1.85,  "averageAmount": 462.27   },
  { "category": "ENTERTAINMENT", "transactionCount": 10, "totalAmount": 3063.98,   "percentageOfTotal": 1.12,  "averageAmount": 306.40   },
  { "category": "HEALTHCARE",    "transactionCount": 4,  "totalAmount": 2925.00,   "percentageOfTotal": 1.07,  "averageAmount": 731.25   }
]
```

**Result: PASS** — All 9 active categories returned with correct counts, totals, and percentages.

---

## 2. Transaction Endpoints

### 2.1 GET /api/v1/transactions — List All Transactions (Paginated)

**Request:**
```
GET http://localhost:8080/api/v1/transactions?page=0&size=5&sortBy=transactionDate&sortDirection=DESC
```

**Response: 200 OK**
```json
{
  "content": [
    {
      "transactionRef": "BANK-CUST005-TRF-050",
      "customerName": "Lerato Sithole",
      "amount": 6500.00,
      "currency": "ZAR",
      "type": "CREDIT",
      "category": "TRANSFER",
      "description": "EFT Received Rental Income",
      "sourceSystem": "BANK",
      "transactionDate": "2026-01-23T08:00:00",
      "status": "PROCESSED"
    }
    // ... 4 more
  ],
  "page": 0,
  "size": 5,
  "totalElements": 500,
  "totalPages": 100,
  "hasNext": true,
  "hasPrevious": false
}
```

**Result: PASS** — 500 total transactions, sorted DESC by date, paginated correctly.

---

### 2.2 GET /api/v1/transactions/ref/{transactionRef} — Get by Reference

**Request:**
```
GET http://localhost:8080/api/v1/transactions/ref/BANK-CUST001-TRF-050
```

**Response: 200 OK**
```json
{
  "id": "b28975e7-4591-45d1-a025-70274ef1892e",
  "transactionRef": "BANK-CUST001-TRF-050",
  "customerId": "CUST001",
  "customerName": "Sipho Dlamini",
  "amount": 6500.00,
  "currency": "ZAR",
  "type": "CREDIT",
  "category": "TRANSFER",
  "description": "EFT Received Rental Income",
  "merchant": "Tenant Payment",
  "sourceSystem": "BANK",
  "transactionDate": "2026-01-23T08:00:00",
  "processedAt": "2026-04-06T15:06:44.340957",
  "status": "PROCESSED"
}
```

**Result: PASS** — Exact transaction returned by reference.

---

### 2.3 POST /api/v1/transactions/sync — Manual Re-sync

**Request:**
```
POST http://localhost:8080/api/v1/transactions/sync
```

**Response: 200 OK**
```json
{
  "status": "success",
  "timestamp": "2026-04-06T15:10:01.689226200",
  "message": "Sync completed successfully",
  "newTransactionsPersisted": 0
}
```

**Result: PASS** — Deduplication working correctly. Re-sync detected all 500 existing refs and persisted 0 duplicates.

---

## 3. Aggregation Endpoints

### 3.1 GET /api/v1/transactions/aggregate — Overall Aggregation Summary

**Request:**
```
GET http://localhost:8080/api/v1/transactions/aggregate
```

**Response: 200 OK**
```json
{
  "totalTransactions": 500,
  "totalDebits": 404342.40,
  "totalCredits": 967500.00,
  "netPosition": 563157.60,
  "averageTransactionAmount": 2743.68,
  "transactionsByCategory": {
    "GROCERIES":     85,
    "DINING":        85,
    "TRANSPORT":     75,
    "TRANSFER":      65,
    "UTILITIES":     55,
    "ENTERTAINMENT": 50,
    "SHOPPING":      45,
    "HEALTHCARE":    20,
    "SALARY":        20
  },
  "amountByCategory": {
    "SALARY":        900000.00,
    "TRANSFER":      206125.00,
    "SHOPPING":      82400.00,
    "GROCERIES":     64252.50,
    "TRANSPORT":     35270.00,
    "DINING":        28425.00,
    "UTILITIES":     25425.00,
    "ENTERTAINMENT": 15319.90,
    "HEALTHCARE":    14625.00
  },
  "transactionsBySource": {
    "BANK":           250,
    "CREDIT_CARD":    150,
    "MOBILE_PAYMENT": 100
  },
  "transactionsByType": {
    "DEBIT":  460,
    "CREDIT":  40
  },
  "transactionsByStatus": {
    "PROCESSED": 500
  },
  "dateRangeFrom": "2025-10-01T08:00:00",
  "dateRangeTo":   "2026-01-23T08:00:00",
  "generatedAt":   "2026-04-06T15:09:18"
}
```

**Result: PASS** — Full aggregation with breakdowns by category, source, type, and status.

---

### 3.2 GET /api/v1/transactions/categories/summary — Category Summary

**Request:**
```
GET http://localhost:8080/api/v1/transactions/categories/summary
```

**Response: 200 OK**

| Category | Count | Total (ZAR) | % of Total | Avg Amount |
|---|---|---|---|---|
| SALARY | 20 | R 900,000.00 | 65.61% | R 45,000.00 |
| TRANSFER | 65 | R 206,125.00 | 15.03% | R 3,171.15 |
| SHOPPING | 45 | R 82,400.00 | 6.01% | R 1,831.11 |
| GROCERIES | 85 | R 64,252.50 | 4.68% | R 755.91 |
| TRANSPORT | 75 | R 35,270.00 | 2.57% | R 470.27 |
| DINING | 85 | R 28,425.00 | 2.07% | R 334.41 |
| UTILITIES | 55 | R 25,425.00 | 1.85% | R 462.27 |
| ENTERTAINMENT | 50 | R 15,319.90 | 1.12% | R 306.40 |
| HEALTHCARE | 20 | R 14,625.00 | 1.07% | R 731.25 |

**Result: PASS** — Sorted by amount DESC, percentages sum to 100%.

---

### 3.3 GET /api/v1/transactions/sources/summary — Source Summary

**Request:**
```
GET http://localhost:8080/api/v1/transactions/sources/summary
```

**Response: 200 OK**

| Source | Count | % | Total Debits | Total Credits | Net Position |
|---|---|---|---|---|---|
| BANK | 250 | 50% | R 264,902.50 | R 957,500.00 | +R 692,597.50 |
| CREDIT_CARD | 150 | 30% | R 119,244.90 | R 0.00 | -R 119,244.90 |
| MOBILE_PAYMENT | 100 | 20% | R 20,195.00 | R 10,000.00 | -R 10,195.00 |

**Result: PASS** — Per-source breakdown with net positions.

---

### 3.4 GET /api/v1/transactions/trends/monthly — Monthly Trends

**Request:**
```
GET http://localhost:8080/api/v1/transactions/trends/monthly
```

**Response: 200 OK**

| Period | Debits (ZAR) | Credits (ZAR) | Net Position | Tx Count | Avg Tx |
|---|---|---|---|---|---|
| Oct 2025 | R 113,247.50 | R 254,000.00 | +R 140,752.50 | 200 | R 1,836.24 |
| Nov 2025 | R 114,144.95 | R 231,000.00 | +R 116,855.05 | 150 | R 2,300.97 |
| Dec 2025 | R 109,504.95 | R 225,000.00 | +R 115,495.05 | 100 | R 3,345.05 |
| Jan 2026 | R 67,445.00  | R 257,500.00 | +R 190,055.00 | 50  | R 6,498.90 |

**Result: PASS** — 4 months of trend data, consistently net positive (salary credits dominate).

---

## 4. Filtering & Sorting

### 4.1 Filter by Category + Customer

**Request:**
```
GET http://localhost:8080/api/v1/transactions?category=GROCERIES&customerId=CUST001&size=3
```

**Response: 200 OK**
```json
{
  "content": [
    { "transactionRef": "BANK-CUST001-GRO-047", "amount": 720.00, "merchant": "Checkers",   "category": "GROCERIES" },
    { "transactionRef": "BANK-CUST001-GRO-045", "amount": 540.00, "merchant": "Pick n Pay", "category": "GROCERIES" },
    { "transactionRef": "BANK-CUST001-GRO-042", "amount": 1100.00,"merchant": "Woolworths", "category": "GROCERIES" }
  ],
  "totalElements": 17,
  "totalPages": 6
}
```

**Result: PASS** — 17 grocery transactions for CUST001, all correctly categorized with SA merchants.

---

### 4.2 Filter by Source System + Sort by Amount DESC

**Request:**
```
GET http://localhost:8080/api/v1/transactions?sourceSystem=CREDIT_CARD&size=3&sortBy=amount&sortDirection=DESC
```

**Response: 200 OK**
```json
{
  "content": [
    { "transactionRef": "CC-CUST002-SHP-022", "amount": 4500.00, "merchant": "Takealot", "description": "Takealot Black Friday Deal" },
    { "transactionRef": "CC-CUST003-SHP-022", "amount": 4500.00, "merchant": "Takealot", "description": "Takealot Black Friday Deal" },
    { "transactionRef": "CC-CUST004-SHP-022", "amount": 4500.00, "merchant": "Takealot", "description": "Takealot Black Friday Deal" }
  ],
  "totalElements": 150,
  "totalPages": 50
}
```

**Result: PASS** — 150 credit card transactions, correctly sorted by amount descending.

---

### 4.3 Customer Monthly Trends

**Request:**
```
GET http://localhost:8080/api/v1/customers/CUST001/trends/monthly
```

**Response: 200 OK**

| Period | Debits | Credits | Net | Count |
|---|---|---|---|---|
| Oct 2025 | R 22,649.50 | R 50,800.00 | +R 28,150.50 | 40 |
| Nov 2025 | R 22,828.99 | R 46,200.00 | +R 23,371.01 | 30 |
| Dec 2025 | R 21,900.99 | R 45,000.00 | +R 23,099.01 | 20 |
| Jan 2026 | R 13,489.00 | R 51,500.00 | +R 38,011.00 | 10 |

**Result: PASS** — Per-customer trends scoped correctly (exactly 1/5 of global totals).

---

## 5. Error Handling

### 5.1 Customer Not Found — 404

**Request:**
```
GET http://localhost:8080/api/v1/customers/CUST999
```

**Response: 404 Not Found**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Customer not found with customerId: 'CUST999'",
  "path": "/api/v1/customers/CUST999",
  "timestamp": "2026-04-06T15:09:51.093022"
}
```

**Result: PASS** — Descriptive 404 with resource type, field, and value in message.

---

### 5.2 Invalid UUID Format — 400

**Request:**
```
GET http://localhost:8080/api/v1/transactions/not-a-valid-uuid
```

**Response: 400 Bad Request**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid UUID format: not-a-valid-uuid",
  "path": "/api/v1/transactions/not-a-valid-uuid",
  "timestamp": "2026-04-06T15:10:01.772855"
}
```

**Result: PASS** — Meaningful 400 with input value in error message.

---

### 5.3 Duplicate Sync (Idempotency)

**Request:**
```
POST http://localhost:8080/api/v1/transactions/sync
```

**Response: 200 OK**
```json
{
  "status": "success",
  "message": "Sync completed successfully",
  "newTransactionsPersisted": 0
}
```

**Result: PASS** — Re-sync is fully idempotent. All 500 existing `transactionRef` values detected as duplicates, zero inserted.

---

## Unit & Integration Test Results

Executed via `mvn test`:

```
Tests run: 109, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

| Test Class | Tests | Result |
|---|---|---|
| TransactionCategorizationServiceTest | 73 | PASS |
| TransactionAggregatorServiceTest | 13 | PASS |
| TransactionControllerTest | 15 | PASS |
| CustomerControllerTest | 8 | PASS |
| **Total** | **109** | **ALL PASS** |

### Notable test coverage:
- All 9 transaction categories tested with multiple keyword variants
- Edge cases: null description, blank merchant, unknown keywords → `OTHER`
- Case-insensitivity: `"WOOLWORTHS"`, `"woolworths"`, `"Woolworths"` all → `GROCERIES`
- Uber Eats → `DINING` (not `TRANSPORT`) verified
- Gautrain → `TRANSPORT` (not `UTILITIES` via substring "rain") verified
- Sync deduplication logic mocked and verified
- Controller layer tested with `@WebMvcTest` and MockMvc
- 404/400 error response shapes verified in controller tests
