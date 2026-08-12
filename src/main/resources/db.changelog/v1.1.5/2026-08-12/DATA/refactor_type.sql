ALTER TABLE orders
ALTER COLUMN payment_method_list TYPE jsonb
USING payment_method_list::jsonb;