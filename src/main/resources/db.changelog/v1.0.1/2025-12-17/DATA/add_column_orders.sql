ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS url_to_return VARCHAR(255),                -- URL для перехода после успешной оплаты
    ADD COLUMN IF NOT EXISTS url_to_decline VARCHAR(255),               -- URL для перехода после неуспешной оплаты
    ADD COLUMN IF NOT EXISTS reg_card BOOLEAN DEFAULT FALSE,            -- ФИО страхователя
    ADD COLUMN IF NOT EXISTS skip_sending_queue BOOLEAN DEFAULT TRUE,   -- Пропустить отправку чека
    ADD COLUMN IF NOT EXISTS skip_sending_receipt BOOLEAN DEFAULT TRUE, -- Пропустить отправку результата платежа в очередь
    ADD COLUMN IF NOT EXISTS queue_status_result_name VARCHAR(255),     -- Наименование очереди для отправки статуса оплаты
    ADD COLUMN IF NOT EXISTS refund_date TIMESTAMP;                     -- Дата возврата средств