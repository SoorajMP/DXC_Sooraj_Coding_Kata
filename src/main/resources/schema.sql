CREATE TABLE IF NOT EXISTS accounts (
    id UUID PRIMARY KEY,
    balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
    );

CREATE TABLE IF NOT EXISTS banking_transactions (
    id UUID PRIMARY KEY,
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    source_account_id UUID,
    destination_account_id UUID,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_transaction_source_account
    FOREIGN KEY (source_account_id) REFERENCES accounts(id),

    CONSTRAINT fk_transaction_destination_account
    FOREIGN KEY (destination_account_id) REFERENCES accounts(id)
    );

CREATE TABLE IF NOT EXISTS ledger_entries (
      id UUID PRIMARY KEY,
      account_id UUID NOT NULL,
      transaction_id UUID NOT NULL,
      entry_type VARCHAR(10) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    balance_after DECIMAL(19, 2) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT chk_ledger_entry_type
    CHECK (entry_type IN ('DEBIT', 'CREDIT')),

    CONSTRAINT fk_ledger_account
    FOREIGN KEY (account_id) REFERENCES accounts(id),

    CONSTRAINT fk_ledger_transaction
    FOREIGN KEY (transaction_id) REFERENCES banking_transactions(id)
    );

CREATE INDEX IF NOT EXISTS idx_ledger_account_time
    ON ledger_entries(account_id);

CREATE INDEX IF NOT EXISTS idx_transaction_source
    ON banking_transactions(source_account_id);

CREATE INDEX IF NOT EXISTS idx_transaction_destination
    ON banking_transactions(destination_account_id);