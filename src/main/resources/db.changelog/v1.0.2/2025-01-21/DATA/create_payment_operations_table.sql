CREATE TABLE payment_operations (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    state VARCHAR(65) NOT NULL,
    bank VARCHAR(65) NOT NULL,
    type VARCHAR(65) NOT NULL,
    depersonalization BOOLEAN NOT NULL DEFAULT false,
    pay_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    update_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
);