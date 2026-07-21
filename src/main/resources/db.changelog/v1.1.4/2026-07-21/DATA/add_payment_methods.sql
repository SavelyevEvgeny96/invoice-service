ALTER TABLE orders
    ADD COLUMN payment_method_list JSONB,
    ADD COLUMN bank_qr JSONB;

CREATE TABLE default_payment_methods (
    id UUID PRIMARY KEY,
    name VARCHAR(64) NOT NULL UNIQUE,
    availability BOOLEAN NOT NULL DEFAULT FALSE,
    create_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    update_date TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO default_payment_methods (id, name, availability) VALUES
    ('9d8f7a2b-79ef-4f90-9ae9-87ae31e4c001', 'CARD', TRUE),
    ('9d8f7a2b-79ef-4f90-9ae9-87ae31e4c002', 'SBP', TRUE),
    ('9d8f7a2b-79ef-4f90-9ae9-87ae31e4c003', 'QR_BANKING_DETAILS', FALSE);
