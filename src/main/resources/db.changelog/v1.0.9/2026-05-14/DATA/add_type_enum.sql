--changeset author:add-reversal-to-order-status-enum
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_enum e
        JOIN pg_type t ON t.oid = e.enumtypid
        WHERE t.typname = 'order_statuses_enum'
          AND e.enumlabel = 'REVERSAL'
    ) THEN
        ALTER TYPE order_statuses_enum ADD VALUE 'REVERSAL';
    END IF;
END
$$;