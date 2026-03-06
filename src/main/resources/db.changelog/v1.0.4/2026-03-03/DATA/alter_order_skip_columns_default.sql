ALTER TABLE orders
    ALTER COLUMN skip_sending_queue SET DEFAULT false,
    ALTER COLUMN skip_sending_receipt SET DEFAULT false