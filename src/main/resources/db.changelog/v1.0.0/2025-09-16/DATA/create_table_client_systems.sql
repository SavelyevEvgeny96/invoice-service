
CREATE TABLE client_systems (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),  -- Автоинкрементируемый ID
    external_system_code VARCHAR(50) UNIQUE , -- Код системы
    external_system_name VARCHAR(255) NOT NULL -- Наименование системы
);
INSERT INTO client_systems(external_system_code,external_system_name)VALUES('www-sogaz-client','Сайт 2.0');