--liquibase formatted sql

--changeset you:add-refund-enum runInTransaction:false splitStatements:true
ALTER TYPE order_statuses_enum
  ADD VALUE IF NOT EXISTS 'REFUND';

--changeset you:add-refund-description splitStatements:true
INSERT INTO order_status_descriptions (status, comment)
VALUES ('REFUND', 'Возврат средств')
ON CONFLICT (status) DO NOTHING;