--changeset author:add-reversal-order-status-description
INSERT INTO order_status_descriptions(status, comment)
SELECT 'REVERSAL', 'Заказ отменен с возвратом средств'
WHERE NOT EXISTS (
    SELECT 1
    FROM order_status_descriptions
    WHERE status = 'REVERSAL'
);