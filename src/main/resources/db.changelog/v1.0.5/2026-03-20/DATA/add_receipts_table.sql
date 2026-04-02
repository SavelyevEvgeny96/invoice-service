CREATE TABLE receipts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID,
    payment_id UUID NOT NULL,
    state VARCHAR(50) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    type_operation VARCHAR(50) NOT NULL,
    sending_time TIMESTAMP,
    link VARCHAR(255),
    error_text VARCHAR(255),
    create_date TIMESTAMP DEFAULT now(),
    update_date TIMESTAMP DEFAULT now()
);

CREATE INDEX ON receipts(order_id);