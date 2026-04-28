ALTER TABLE payment_operations
    ADD COLUMN IF NOT EXISTS payment_system VARCHAR(255),
    ADD COLUMN IF NOT EXISTS external_error_code VARCHAR(255);