ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS check_payment_information VARCHAR(255);