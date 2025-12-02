CREATE TABLE payment_operation_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),                           -- Автоинкрементируемый ID
    action BIGINT,                                      -- Действие
    action_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- Дата выполнения операции
    action_author_id VARCHAR(255),                      -- Исполнитель
    order_id UUID NOT NULL,                              -- Идентификатор заказа
    FOREIGN KEY (order_id) REFERENCES orders(order_id), -- Связь с заказом
    FOREIGN KEY (action_author_id) REFERENCES client_systems(external_system_code)
);