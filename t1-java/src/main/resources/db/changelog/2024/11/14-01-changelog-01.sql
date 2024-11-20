-- liquibase formatted sql
ALTER TABLE client
    ADD COLUMN client_id UUID UNIQUE NOT NULL;

ALTER TABLE account
    DROP COLUMN balance,
    ADD COLUMN account_id UUID UNIQUE NOT NULL,
    ADD COLUMN status VARCHAR(10) NOT NULL,
    ADD COLUMN frozen_amount NUMERIC(15, 2) NOT NULL DEFAULT 0 CHECK(frozen_amount >= 0),
    ADD COLUMN balance NUMERIC(15, 2) NOT NULL; -- CHECK (balance >= 0),
   -- ADD CONSTRAINT chk_frozen_balance CHECK (frozen_amount <= balance);

ALTER TABLE transaction
    DROP COLUMN amount,
    DROP COLUMN created_at,
    ADD COLUMN transaction_id UUID UNIQUE NOT NULL,
    ADD COLUMN amount NUMERIC(15, 2) NOT NULL CHECK (amount >= 0),
    ADD COLUMN status VARCHAR(10) NOT NULL,
    ADD COLUMN operation_type VARCHAR(10) NOT NULL,
    ADD COLUMN "timestamp" TIMESTAMP NOT NULL;



