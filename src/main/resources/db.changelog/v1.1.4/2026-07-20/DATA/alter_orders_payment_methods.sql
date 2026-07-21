ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS payment_method_list VARCHAR(255),
    ADD COLUMN IF NOT EXISTS payer_last_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS payer_first_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS payer_middle_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS check_url_return BOOLEAN;
