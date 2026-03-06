ALTER TABLE orders
    ADD COLUMN receipt_state VARCHAR(65),
    ADD COLUMN depersonalization BOOLEAN DEFAULT FALSE
