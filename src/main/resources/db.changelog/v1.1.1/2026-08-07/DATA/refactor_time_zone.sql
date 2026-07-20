ALTER TABLE payment_operations
ALTER COLUMN pay_date TYPE timestamp without time zone
USING pay_date AT TIME ZONE 'Europe/Moscow';