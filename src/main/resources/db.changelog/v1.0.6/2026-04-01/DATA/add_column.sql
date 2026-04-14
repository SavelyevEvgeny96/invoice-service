ALTER TABLE sub_orders
    ADD COLUMN IF NOT EXISTS type_operation VARCHAR(255);
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS type_payment_operation VARCHAR(255);