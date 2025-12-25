ALTER TABLE client_systems
    ADD COLUMN IF NOT EXISTS permission_return boolean NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS skip_sending_errors_queue boolean NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS create_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    ADD COLUMN IF NOT EXISTS update_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now();

INSERT INTO client_systems (
    external_system_code,
    external_system_name,
    permission_return,
    skip_sending_errors_queue
)
VALUES
    ('www-sogaz-client', 'Сайт 2.0', false, false),
    ('adinsure-client',  'Адакта', false, true),
    ('ordering-client',  'Сервис подписок', false, true),
    ('ant-client',       'НСиБ, ВПМК', false, true),
    ('osago-backend',    'ОСАГО бэк', true, false),
    ('lk-sogaz-client',  'Личный кабинет', false, false)
ON CONFLICT (external_system_code) DO UPDATE
SET
    external_system_name = EXCLUDED.external_system_name,
    permission_return = EXCLUDED.permission_return,
    skip_sending_errors_queue = EXCLUDED.skip_sending_errors_queue,
    update_date = now();