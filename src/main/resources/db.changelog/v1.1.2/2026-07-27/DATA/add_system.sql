INSERT INTO client_systems (
    external_system_code,
    external_system_name,
    permission_return,
    skip_sending_errors_queue
)
VALUES
    ('front-sogaz-life-client', 'Сайт СЖ', false, false)
ON CONFLICT (external_system_code) DO UPDATE
SET external_system_name = EXCLUDED.external_system_name,
    permission_return = EXCLUDED.permission_return,
    skip_sending_errors_queue = EXCLUDED.skip_sending_errors_queue,
    update_date = now();

INSERT INTO client_systems (
    external_system_code,
    external_system_name,
    permission_return,
    skip_sending_errors_queue
)
VALUES
    ('bfront-sogaz-life', '1С Бестфронт', false, false)
ON CONFLICT (external_system_code) DO UPDATE
SET external_system_name = EXCLUDED.external_system_name,
    permission_return = EXCLUDED.permission_return,
    skip_sending_errors_queue = EXCLUDED.skip_sending_errors_queue,
    update_date = now();

INSERT INTO client_systems (
    external_system_code,
    external_system_name,
    permission_return,
    skip_sending_errors_queue
)
VALUES
    ('soc-sogaz-client', '1С СОЦ', false, false)
ON CONFLICT (external_system_code) DO UPDATE
SET external_system_name = EXCLUDED.external_system_name,
    permission_return = EXCLUDED.permission_return,
    skip_sending_errors_queue = EXCLUDED.skip_sending_errors_queue,
    update_date = now();
