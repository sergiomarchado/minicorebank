CREATE TYPE currency_code AS ENUM ('EUR','USD','GBP');

CREATE TABLE customers (
    id UUID PRIMARY KEY,
    full_name VARCHAR(200) NOT NULL,
    email VARCHAR(200) UNIQUE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES customers(id),
    iban VARCHAR(34) UNIQUE NOT NULL,
    currency currency_code NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE ledger_entries (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES accounts(id),
    txn_id UUID NOT NULL,
    direction VARCHAR(6) NOT NULL, -- 'DEBIT' o 'CREDIT'
    amount_minor BIGINT NOT NULL CHECK (amount_minor > 0),
    currency currency_code NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_ledger_account ON ledger_entries(account_id);
CREATE INDEX idx_ledger_txn ON ledger_entries(txn_id);

CREATE TABLE transfers (
    id UUID PRIMARY KEY,
    from_account UUID NOT NULL REFERENCES accounts(id),
    to_account UUID NOT NULL REFERENCES accounts(id),
    amount_minor BIGINT NOT NULL CHECK (amount_minor > 0),
    currency currency_code NOT NULL,
    status VARCHAR(20) NOT NULL, -- 'COMPLETED','FAILED'
    reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE idempotency_keys (
    key VARCHAR(80) PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    response_txn_id UUID
);
