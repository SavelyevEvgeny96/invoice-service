INSERT INTO client_systems (
    external_system_code,
    external_system_name,
    permission_return,
    skip_sending_errors_queue
)
VALUES
    ('lk-sogaz-life-client', 'СЖ', false, false)
ON CONFLICT (external_system_code) DO UPDATE
SET external_system_name = EXCLUDED.external_system_name,
    permission_return = EXCLUDED.permission_return,
    skip_sending_errors_queue = EXCLUDED.skip_sending_errors_queue,
    update_date = now();