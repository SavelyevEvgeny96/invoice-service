ALTER TABLE payment_operations
    ALTER COLUMN payment_id
    TYPE UUID
    USING payment_id::uuid;

