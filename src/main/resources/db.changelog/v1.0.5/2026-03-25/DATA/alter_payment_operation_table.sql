ALTER TABLE payment_operations
    ADD COLUMN amount DECIMAL(10, 2),
    ADD COLUMN operation VARCHAR(255),
    ADD COLUMN payment_bank_id VARCHAR(255),
    ADD COLUMN payment_id VARCHAR(255),
    ADD COLUMN pan VARCHAR(255),
    ADD COLUMN payer_ip VARCHAR(255);

