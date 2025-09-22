-- V2: Alinear esquema con JPA (quitar ENUM, renombrar columnas y añadir índices/constraints)

-- 1) CUSTOMERS: renombrar full_name -> name y ajustar longitudes
ALTER TABLE customers RENAME COLUMN full_name TO name;
ALTER TABLE customers ALTER COLUMN name TYPE VARCHAR(120);
ALTER TABLE customers ALTER COLUMN email TYPE VARCHAR(255);

-- Índice por email (si no existiera ya)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_customers_email') THEN
        CREATE INDEX idx_customers_email ON customers(email);
    END IF;
END$$;

-- 2) ACCOUNTS: pasar currency de ENUM a VARCHAR(3) y añadir índice por customer_id
-- Si la columna es del tipo ENUM currency_code, la convertimos a texto y luego a VARCHAR(3)
ALTER TABLE accounts ALTER COLUMN currency TYPE VARCHAR(3) USING currency::text;

-- Índice por customer_id
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_accounts_customer_id') THEN
        CREATE INDEX idx_accounts_customer_id ON accounts(customer_id);
    END IF;
END$$;

-- 3) LEDGER_ENTRIES: currency a VARCHAR(3), UNIQUE en txn_id e índices útiles
ALTER TABLE ledger_entries ALTER COLUMN currency TYPE VARCHAR(3) USING currency::text;

-- Unique por idempotencia
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_ledger_entries_txn_id'
    ) THEN
        ALTER TABLE ledger_entries
            ADD CONSTRAINT uk_ledger_entries_txn_id UNIQUE (txn_id);
    END IF;
END$$;

-- Índice por created_at (si no existe)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_ledger_entries_created_at') THEN
        CREATE INDEX idx_ledger_entries_created_at ON ledger_entries(created_at);
    END IF;
END$$;

-- Índice por account_id (tu script ya creaba idx_ledger_account; lo dejamos si existe)
-- (opcional) eliminar el índice suelto por txn y quedarnos con el UNIQUE (ya sirve como índice)
-- DROP INDEX IF EXISTS idx_ledger_txn;

-- 4) TRANSFERS: currency a VARCHAR(3) por coherencia con JPA
ALTER TABLE transfers ALTER COLUMN currency TYPE VARCHAR(3) USING currency::text;

-- 5) Eliminar el tipo ENUM si ya no lo usa ninguna tabla
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns WHERE udt_name = 'currency_code'
    ) THEN
        DROP TYPE IF EXISTS currency_code;
    END IF;
END$$;
