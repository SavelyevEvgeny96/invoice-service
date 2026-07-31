ALTER TABLE orders
    ADD COLUMN payer_last_name VARCHAR(255),
    ADD COLUMN payer_first_name VARCHAR(255),
    ADD COLUMN payer_middle_name VARCHAR(255),
    ADD COLUMN check_url_return BOOLEAN;
