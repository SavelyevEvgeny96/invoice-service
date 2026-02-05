ALTER TYPE order_statuses_enum
ADD VALUE IF NOT EXISTS 'REFUND';

INSERT INTO order_status_descriptions (status, comment)
VALUES ('REFUND', 'Возврат средств')
ON CONFLICT (status) DO NOTHING;