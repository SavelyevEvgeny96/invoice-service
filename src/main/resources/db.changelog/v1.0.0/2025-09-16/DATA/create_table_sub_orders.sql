CREATE TABLE sub_orders (
   sub_order_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),     -- GUID ID подзаказа
   order_id UUID NOT NULL,                      -- GUID ID заказа
   policy_id VARCHAR(255) NOT NULL,             -- Идентификатор полиса
   policy_number VARCHAR(255) NOT NULL,         -- Номер полиса
   policy_date TIMESTAMP,                       -- Дата создания полиса
   contract_id VARCHAR(255),                    -- Идентификатор договора
   contract_number VARCHAR(255) NOT NULL,       -- Номер договора
   contract_date TIMESTAMP,                     -- Дата заключения договора
   insurance_program VARCHAR(255),              -- Программа страхования
   type_insurance VARCHAR(255),                 -- Вид страхования
   doc_type VARCHAR(255),                       -- Тип документа
   premium_amount VARCHAR(255),                 -- Размер премии
   manager_email VARCHAR(255),                  -- Электронная почта менеджера
   channel VARCHAR(255),                        -- Канал, в котором создается счет
   main_contract_check BOOLEAN DEFAULT FALSE,   -- Основной договор страхования
   create_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(), -- Дата создания
   update_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),  -- Дата обновления
   FOREIGN KEY (order_id)REFERENCES orders(order_id));