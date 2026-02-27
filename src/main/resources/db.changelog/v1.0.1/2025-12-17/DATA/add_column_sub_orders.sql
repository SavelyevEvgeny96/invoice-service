ALTER TABLE sub_orders
    ADD COLUMN IF NOT EXISTS send_status_product BOOLEAN DEFAULT FALSE;       -- Отправить статус в продуктовый сервис