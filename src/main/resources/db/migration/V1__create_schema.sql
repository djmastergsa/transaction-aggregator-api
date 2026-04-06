-- ============================================================
-- V1 - Initial schema
-- Creates customers and transactions tables with all indexes.
-- Compatible with H2 (dev/test) and PostgreSQL (production).
-- ============================================================

CREATE TABLE customers (
    id          UUID         NOT NULL,
    customer_id VARCHAR(20)  NOT NULL,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    phone       VARCHAR(20),
    created_at  TIMESTAMP,

    CONSTRAINT pk_customers          PRIMARY KEY (id),
    CONSTRAINT uq_customers_id       UNIQUE (customer_id),
    CONSTRAINT uq_customers_email    UNIQUE (email)
);

CREATE INDEX idx_customer_customer_id ON customers (customer_id);
CREATE INDEX idx_customer_email       ON customers (email);

-- ============================================================

CREATE TABLE transactions (
    id               UUID           NOT NULL,
    transaction_ref  VARCHAR(50)    NOT NULL,
    customer_id      UUID           NOT NULL,
    amount           DECIMAL(19, 4) NOT NULL,
    type             VARCHAR(10)    NOT NULL,
    category         VARCHAR(20)    NOT NULL,
    description      VARCHAR(500),
    merchant         VARCHAR(200),
    source_system    VARCHAR(30)    NOT NULL,
    transaction_date TIMESTAMP      NOT NULL,
    processed_at     TIMESTAMP,
    currency         VARCHAR(3)     DEFAULT 'ZAR',
    status           VARCHAR(10)    NOT NULL,

    CONSTRAINT pk_transactions          PRIMARY KEY (id),
    CONSTRAINT uq_transactions_ref      UNIQUE (transaction_ref),
    CONSTRAINT fk_transactions_customer FOREIGN KEY (customer_id) REFERENCES customers (id)
);

CREATE UNIQUE INDEX idx_transaction_ref      ON transactions (transaction_ref);
CREATE INDEX        idx_transaction_customer ON transactions (customer_id);
CREATE INDEX        idx_transaction_date     ON transactions (transaction_date);
CREATE INDEX        idx_transaction_category ON transactions (category);
CREATE INDEX        idx_transaction_type     ON transactions (type);
CREATE INDEX        idx_transaction_source   ON transactions (source_system);
